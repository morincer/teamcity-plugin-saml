package jetbrains.buildServer.auth.saml.plugin;

import com.onelogin.saml2.settings.Metadata;
import com.onelogin.saml2.settings.Saml2Settings;
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlAttributeMappingSettings;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.groups.SUserGroup;
import jetbrains.buildServer.groups.UserGroup;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.xerces.impl.dv.util.Base64;
import org.hamcrest.CoreMatchers;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class SamlAuthenticationSchemeTest {

    SamlAuthenticationScheme scheme;
    private InMemorySamlPluginSettingsStorage settingsStorage;
    private UserModel userModel;
    private UserGroupManager userGroupManager;
    private SUser validUser;
    private SUser newUser;
    private RootUrlHolder rootUrlHolder;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BasicConfigurator.configure();
    }

    @Before
    public void setUp() throws Exception {
        this.settingsStorage = new InMemorySamlPluginSettingsStorage();

        this.validUser = mock(SUser.class);
        when(validUser.getUsername()).thenReturn("valid_user");
        when(validUser.getName()).thenReturn("Valid User");

        this.newUser = mock(SUser.class);
        when(newUser.getUsername()).thenReturn("new_user");
        when(newUser.getName()).thenReturn("New User");

        this.userModel = mock(UserModel.class);
        when(userModel.findUserAccount(null, "valid_user")).thenReturn(validUser);
        when(userModel.createUserAccount(null, "new_user")).thenReturn(newUser);
        when(userModel.createUserAccount(null, "new_user@somemail.com")).thenReturn(newUser);

        this.userGroupManager = mock(UserGroupManager.class);

        this.rootUrlHolder = mock(RootUrlHolder.class);
        when(rootUrlHolder.getRootUrl()).thenReturn("http://server.com");

        this.settingsStorage.settings.setStrict(false);

        LoginConfiguration loginConfiguration = mock(LoginConfiguration.class);

        this.scheme = new SamlAuthenticationScheme(rootUrlHolder, settingsStorage, userModel, userGroupManager, loginConfiguration);
    }

    @After
    public void tearDown() throws Exception {
        Mockito.reset();
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldGenerateValidCallbackUrl() throws MalformedURLException {
        when(this.rootUrlHolder.getRootUrl()).thenReturn("http://dev.lan");
        var callbackUrl = scheme.getCallbackUrl();
        assertThat(callbackUrl.toString(), equalTo("http://dev.lan/app/saml/callback/"));

        when(this.rootUrlHolder.getRootUrl()).thenReturn("http://dev.lan/");
        callbackUrl = scheme.getCallbackUrl();
        assertThat(callbackUrl.toString(), equalTo("http://dev.lan/app/saml/callback/"));

        when(this.rootUrlHolder.getRootUrl()).thenReturn("https://dev.lan/teamcity");
        callbackUrl = scheme.getCallbackUrl();
        assertThat(callbackUrl.toString(), equalTo("https://dev.lan/teamcity/app/saml/callback/"));

        when(this.rootUrlHolder.getRootUrl()).thenReturn("https://dev.lan/teamcity/");
        callbackUrl = scheme.getCallbackUrl();
        assertThat(callbackUrl.toString(), equalTo("https://dev.lan/teamcity/app/saml/callback/"));
    }

    @Test
    public void shouldAuthenticateValidSamlClaimForValidUser() throws IOException, XPathException, CertificateEncodingException {
        var request = mock(HttpServletRequest.class);
        var samlResponsePath = "src/test/resources/saml_signed_message.xml";

        var callbackUrl = "http://sp.example.com/demo1/index.php?acs";
        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        createSettings();

        var result = simulateSAMLResponse(samlResponsePath, null, null, null);
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));
    }

    @Test
    public void whenCreationOfNewUsersIsAllowedShouldCreateUserForValidClaim() throws IOException {
        var request = mock(HttpServletRequest.class);
        var samlResponsePath = "src/test/resources/saml_signed_new_user.xml";

        var callbackUrl = "http://sp.example.com/demo1/index.php?acs";
        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        createSettings();

        // built using https://capriza.github.io/samling/samling.html#samlPropertiesTab
        var saml = FileUtils.readFileToString(Paths.get(samlResponsePath).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());

        var parameterMap = new HashMap<String, String[]>();
        parameterMap.put("SAMLResponse", new String[] {saml});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("SAMLResponse")).thenReturn(saml);
        var response = mock(HttpServletResponse.class);

        var result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.UNAUTHENTICATED));

        var settings = settingsStorage.load();
        settings.setCreateUsersAutomatically(true);
        settingsStorage.save(settings);

        result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));

        verify(userModel).createUserAccount(null, "new_user");
    }

    @Test
    public void supportsLimitingNewUsersByPostfixes() throws IOException {
        var request = mock(HttpServletRequest.class);
        var samlResponsePathNoMail = "src/test/resources/saml_signed_new_user.xml";
        var samlResponsePathMail = "src/test/resources/saml_signed_new_user_somemail.com.xml";

        var callbackUrl = "http://sp.example.com/demo1/index.php?acs";
        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        createSettings();

        // built using https://capriza.github.io/samling/samling.html#samlPropertiesTab
        var saml = FileUtils.readFileToString(Paths.get(samlResponsePathNoMail).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());

        var parameterMap = new HashMap<String, String[]>();
        parameterMap.put("SAMLResponse", new String[] {saml});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("SAMLResponse")).thenReturn(saml);
        var response = mock(HttpServletResponse.class);

        var settings = settingsStorage.load();
        settings.setCreateUsersAutomatically(true);
        settings.setLimitToPostfixes(true);
        settings.setAllowedPostfixes("@somemail.com");
        settingsStorage.save(settings);

        var result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.UNAUTHENTICATED));

        saml = FileUtils.readFileToString(Paths.get(samlResponsePathMail).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());
        parameterMap.put("SAMLResponse", new String[] {saml});

        result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));

        verify(userModel).createUserAccount(null, "new_user@somemail.com");
    }

    @Test
    public void allowsGettingNewUserFieldsFromSamlAttributes() throws IOException {
        var request = mock(HttpServletRequest.class);
        var samlResponsePath = "src/test/resources/saml_signed_with_attributes.xml";

        var callbackUrl = "http://sp.example.com/demo1/index.php?acs";
        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        createSettings();

        // built using https://capriza.github.io/samling/samling.html#samlPropertiesTab
        var saml = FileUtils.readFileToString(Paths.get(samlResponsePath).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());

        var parameterMap = new HashMap<String, String[]>();
        parameterMap.put("SAMLResponse", new String[] {saml});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("SAMLResponse")).thenReturn(saml);
        var response = mock(HttpServletResponse.class);

        var userMock = mock(SUser.class);

        String userNameId = "User_With_Attributes";
        String userNameName = "User Attributes";
        when(userModel.createUserAccount(null, userNameId)).thenReturn(userMock);
        when(userMock.getUsername()).thenReturn(userNameId);
        when(userMock.getName()).thenReturn(userNameName);

        // For name id
        var settings = settingsStorage.load();
        settings.setCreateUsersAutomatically(true);
        settings.getNameAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
        settings.getEmailAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
        settings.setStrict(false);
        settingsStorage.save(settings);

        var result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));

        verify(userMock).updateUserAccount(userNameId, userNameId, userNameId);

        // For custom attribute
        settings = settingsStorage.load();
        settings.getNameAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_OTHER);
        settings.getNameAttributeMapping().setCustomAttributeName("fullname");
        settings.getEmailAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_OTHER);
        settings.getEmailAttributeMapping().setCustomAttributeName("email");
        settingsStorage.save(settings);

        result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));

        verify(userMock).updateUserAccount(userNameId, "Full Name", "myemail.com");
    }

    @Test
    public void assignsUserGroupsFromSamlAttributes() throws IOException {
        var request = mock(HttpServletRequest.class);
        var samlResponsePath = "src/test/resources/saml_signed_with_groups_attribute.xml";

        var callbackUrl = "http://sp.example.com/demo1/index.php?acs";
        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        createSettings();

        // built using https://capriza.github.io/samling/samling.html#samlPropertiesTab
        var saml = FileUtils.readFileToString(Paths.get(samlResponsePath).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());

        var parameterMap = new HashMap<String, String[]>();
        parameterMap.put("SAMLResponse", new String[] {saml});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("SAMLResponse")).thenReturn(saml);
        var response = mock(HttpServletResponse.class);

        var userMock = mock(SUser.class);
        var userGroupMock = mock(SUserGroup.class);

        String userNameId = "User_With_Groups";
        String userNameName = "User With Groups";
        when(userModel.createUserAccount(null, userNameId)).thenReturn(userMock);
        when(userMock.getUsername()).thenReturn(userNameId);
        when(userMock.getName()).thenReturn(userNameName);

        // Setup some TeamCity groups
        SUserGroup adminGroupMock = mock(SUserGroup.class);
        when(adminGroupMock.getName()).thenReturn("admin");
        when(adminGroupMock.getKey()).thenReturn("ADMIN");
        Collection<SUserGroup> teamcityGroupsMock = new ArrayList<>();
        teamcityGroupsMock.add(adminGroupMock);

        when(userGroupManager.getUserGroups()).thenReturn(teamcityGroupsMock);

        var settings = settingsStorage.load();
        settings.setCreateUsersAutomatically(true);
        settings.setAssignGroups(true);
        settings.getNameAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
        settings.getEmailAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
        settings.getGroupsAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_OTHER);
        settings.getGroupsAttributeMapping().setCustomAttributeName("groups");
        settingsStorage.save(settings);

        HttpAuthenticationResult result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));

        verify(userMock).updateUserAccount(userNameId, userNameId, userNameId);
        verify(adminGroupMock, times(1)).addUser(userMock);
    }

    @Test
    public void removesUserGroupsFromSamlAttributes() throws IOException {
        var request = mock(HttpServletRequest.class);
        var samlResponsePath = "src/test/resources/saml_signed_with_groups_attribute.xml";

        var callbackUrl = "http://sp.example.com/demo1/index.php?acs";
        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        createSettings();

        // built using https://capriza.github.io/samling/samling.html#samlPropertiesTab
        var saml = FileUtils.readFileToString(Paths.get(samlResponsePath).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());

        var parameterMap = new HashMap<String, String[]>();
        parameterMap.put("SAMLResponse", new String[] {saml});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("SAMLResponse")).thenReturn(saml);
        var response = mock(HttpServletResponse.class);

        String userNameId = "User_With_Groups";
        String userNameName = "User With Groups To Remove";

        // Setup some TeamCity groups
        Collection<SUserGroup> teamcityGroupsMock = new ArrayList<>();

        SUserGroup adminGroupMock = mock(SUserGroup.class);
        when(adminGroupMock.getKey()).thenReturn("ADMIN");
        when(adminGroupMock.getName()).thenReturn("admin");
        when(adminGroupMock.toString()).thenReturn("admin");
        teamcityGroupsMock.add(adminGroupMock);
        SUserGroup removeGroupMock = mock(SUserGroup.class);
        when(removeGroupMock.getKey()).thenReturn("REMOVE_GROUP");
        when(removeGroupMock.getName()).thenReturn("remove_group");
        when(removeGroupMock.toString()).thenReturn("remove_group");
        teamcityGroupsMock.add(removeGroupMock);

        when(userGroupManager.getUserGroups()).thenReturn(teamcityGroupsMock);

        // Setup a valid user with some existing groups
        var validUserWithGroups = mock(SUser.class);
        when(validUserWithGroups.getUsername()).thenReturn(userNameId);
        when(validUserWithGroups.getName()).thenReturn(userNameName);

        List<UserGroup> validUserGroupsMock = new ArrayList<>();
        validUserGroupsMock.add(adminGroupMock);
        validUserGroupsMock.add(removeGroupMock);
        when(validUserWithGroups.getUserGroups()).thenReturn(validUserGroupsMock);

        // Return validUser
        when(userModel.findUserAccount(null, userNameId)).thenReturn(validUserWithGroups);

        // Don't remove group membership when not requested
        var settings = settingsStorage.load();
        settings.setCreateUsersAutomatically(true);
        settings.setAssignGroups(true);
        settings.setRemoveUnassignedGroups(false);
        settings.getNameAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
        settings.getEmailAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
        settings.getGroupsAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_OTHER);
        settings.getGroupsAttributeMapping().setCustomAttributeName("groups");
        settingsStorage.save(settings);

        HttpAuthenticationResult result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));

        verify(removeGroupMock, times(0)).removeUser(validUserWithGroups);

        // Do remove group membership when requested
        settings = settingsStorage.load();
        settings.setCreateUsersAutomatically(true);
        settings.setAssignGroups(true);
        settings.setRemoveUnassignedGroups(true);
        settings.getNameAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
        settings.getEmailAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
        settings.getGroupsAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_OTHER);
        settings.getGroupsAttributeMapping().setCustomAttributeName("groups");
        settingsStorage.save(settings);

        result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));

        verify(removeGroupMock, times(1)).removeUser(validUserWithGroups);
    }

    private void createSettings() throws IOException {
        var settings = new SamlPluginSettings();
        settings.setIssuerUrl("http://idp.example.com/metadata.php");
        settings.setSsoEndpoint("http://idp.example.com/metadata.php");
        settings.setEntityId("http://sp.example.com/demo1/metadata.php");
        settings.setPublicCertificate("-----BEGIN CERTIFICATE-----\n" +
                "MIICpzCCAhACCQDuFX0Db5iljDANBgkqhkiG9w0BAQsFADCBlzELMAkGA1UEBhMC\n" +
                "VVMxEzARBgNVBAgMCkNhbGlmb3JuaWExEjAQBgNVBAcMCVBhbG8gQWx0bzEQMA4G\n" +
                "A1UECgwHU2FtbGluZzEPMA0GA1UECwwGU2FsaW5nMRQwEgYDVQQDDAtjYXByaXph\n" +
                "LmNvbTEmMCQGCSqGSIb3DQEJARYXZW5naW5lZXJpbmdAY2Fwcml6YS5jb20wHhcN\n" +
                "MTgwNTE1MTgxMTEwWhcNMjgwNTEyMTgxMTEwWjCBlzELMAkGA1UEBhMCVVMxEzAR\n" +
                "BgNVBAgMCkNhbGlmb3JuaWExEjAQBgNVBAcMCVBhbG8gQWx0bzEQMA4GA1UECgwH\n" +
                "U2FtbGluZzEPMA0GA1UECwwGU2FsaW5nMRQwEgYDVQQDDAtjYXByaXphLmNvbTEm\n" +
                "MCQGCSqGSIb3DQEJARYXZW5naW5lZXJpbmdAY2Fwcml6YS5jb20wgZ8wDQYJKoZI\n" +
                "hvcNAQEBBQADgY0AMIGJAoGBAJEBNDJKH5nXr0hZKcSNIY1l4HeYLPBEKJLXyAno\n" +
                "FTdgGrvi40YyIx9lHh0LbDVWCgxJp21BmKll0CkgmeKidvGlr3FUwtETro44L+Sg\n" +
                "mjiJNbftvFxhNkgA26O2GDQuBoQwgSiagVadWXwJKkodH8tx4ojBPYK1pBO8fHf3\n" +
                "wOnxAgMBAAEwDQYJKoZIhvcNAQELBQADgYEACIylhvh6T758hcZjAQJiV7rMRg+O\n" +
                "mb68iJI4L9f0cyBcJENR+1LQNgUGyFDMm9Wm9o81CuIKBnfpEE2Jfcs76YVWRJy5\n" +
                "xJ11GFKJJ5T0NEB7txbUQPoJOeNoE736lF5vYw6YKp8fJqPW0L2PLWe9qTn8hxpd\n" +
                "njo3k6r5gXyl8tk=\n" +
                "-----END CERTIFICATE-----\n");

        settings.setStrict(false);
        this.settingsStorage.save(settings);
    }

    @Test
    public void shouldGenerateMetadata() throws IOException, CertificateEncodingException {
        createSettings();
        Metadata metadata = this.scheme.generateSPMetadata();
        assertThat(metadata, is(notNullValue()));
    }

    @Test
    public void shouldParseADFSMetadata() throws IOException, XPathException, CertificateEncodingException {
        var metadataFilePath = "src/test/resources/FederationMetadata.xml";
        var metadataXml = FileUtils.readFileToString(Paths.get(metadataFilePath).toAbsolutePath().toFile());

        SamlPluginSettings settings = new SamlPluginSettings();
        this.scheme.importMetadataIntoSettings(metadataXml, settings);

        assertThat(settings.getPublicCertificate(), is(notNullValue()));
        assertThat(settings.getPublicCertificate(), is(CoreMatchers.containsString("MIIC9j")));
        assertThat(settings.getAdditionalCerts(), is(notNullValue()));
        assertThat(settings.getAdditionalCerts().size(), equalTo(1));
        assertThat(settings.getAdditionalCerts().get(0), is(notNullValue()));
        assertThat(settings.getAdditionalCerts().get(0), is(CoreMatchers.containsString("MIIC8DC")));
    }

    @Test
    public void shouldBuildProperSaml2SettingsWhenMultipleCertificates() throws IOException, CertificateEncodingException, XPathException {
        var metadataFilePath = "src/test/resources/FederationMetadata.xml";
        var metadataXml = FileUtils.readFileToString(Paths.get(metadataFilePath).toAbsolutePath().toFile());

        SamlPluginSettings settings = new SamlPluginSettings();
        this.scheme.importMetadataIntoSettings(metadataXml, settings);

        settings.setEntityId("http://test.entity");

        this.settingsStorage.save(settings);

        Saml2Settings saml2Settings = this.scheme.buildSettings();
        assertThat(saml2Settings.getIdpx509cert(), is(notNullValue()));
        assertThat(saml2Settings.getIdpx509certMulti().size(), equalTo(1));
    }

    @Test
    public void shouldProperlyAuthenticateWhenMultipleCertificates() throws IOException, CertificateEncodingException, XPathException {
        var samlResponsePath = "src/test/resources/adfsSignedMessage.xml";
        var metadataFilePath = "src/test/resources/FederationMetadata.xml";

        when(userModel.findUserAccount(null, "diego.gomes@philips.com")).thenReturn(validUser);

        HttpAuthenticationResult httpAuthenticationResult = simulateSAMLResponse(samlResponsePath, metadataFilePath, null, null);
        assertThat(httpAuthenticationResult.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));;
    }

    private HttpAuthenticationResult simulateSAMLResponse(String samlResponsePath, String metadataFilePath, String callbackUrl, String entityId) throws IOException, CertificateEncodingException, XPathException {
        var request = mock(HttpServletRequest.class);

        if (entityId == null) {
            entityId = "http://test.lan/app/callback";
        }

        if (metadataFilePath != null) {
            var metadataXml = FileUtils.readFileToString(Paths.get(metadataFilePath).toAbsolutePath().toFile());

            SamlPluginSettings settings = new SamlPluginSettings();
            this.scheme.importMetadataIntoSettings(metadataXml, settings);

            settings.setStrict(false);
            settings.setEntityId(entityId);
            this.settingsStorage.save(settings);
        }

        if (callbackUrl == null) {
            callbackUrl = "http://test.lan/app/callback";
        }

        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        // built using https://capriza.github.io/samling/samling.html#samlPropertiesTab
        var saml = FileUtils.readFileToString(Paths.get(samlResponsePath).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());

        var parameterMap = new HashMap<String, String[]>();
        parameterMap.put("SAMLResponse", new String[] {saml});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("SAMLResponse")).thenReturn(saml);
        var response = mock(HttpServletResponse.class);
        var result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        return result;
    }


}
