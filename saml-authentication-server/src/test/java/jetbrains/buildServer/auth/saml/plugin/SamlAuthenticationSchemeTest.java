package jetbrains.buildServer.auth.saml.plugin;

import com.onelogin.saml2.settings.Saml2Settings;
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlAttributeMappingSettings;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import lombok.var;
import org.apache.log4j.BasicConfigurator;
import org.apache.xerces.impl.dv.util.Base64;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.testng.reporters.Files;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathException;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SamlAuthenticationSchemeTest {

    SamlAuthenticationScheme scheme;
    private InMemorySamlPluginSettingsStorage settingsStorage;
    private UserModel userModel;
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

        this.rootUrlHolder = mock(RootUrlHolder.class);
        when(rootUrlHolder.getRootUrl()).thenReturn("http://server.com");

        this.scheme = new SamlAuthenticationScheme(rootUrlHolder, settingsStorage, userModel);
    }

    @After
    public void tearDown() throws Exception {
        Mockito.reset();
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
        var saml = Files.readFile(Paths.get(samlResponsePath).toAbsolutePath().toFile());
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
        var saml = Files.readFile(Paths.get(samlResponsePathNoMail).toAbsolutePath().toFile());
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

        saml = Files.readFile(Paths.get(samlResponsePathMail).toAbsolutePath().toFile());
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
        var saml = Files.readFile(Paths.get(samlResponsePath).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());

        var parameterMap = new HashMap<String, String[]>();
        parameterMap.put("SAMLResponse", new String[] {saml});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("SAMLResponse")).thenReturn(saml);
        var response = mock(HttpServletResponse.class);

        var userMock = mock(SUser.class);

        String userNameId = "User_With_Attributes";
        when(userModel.createUserAccount(null, userNameId)).thenReturn(userMock);
        when(userMock.getUsername()).thenReturn(userNameId);

        // For name id
        var settings = settingsStorage.load();
        settings.setCreateUsersAutomatically(true);
        settings.getNameAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
        settings.getEmailAttributeMapping().setMappingType(SamlAttributeMappingSettings.TYPE_NAME_ID);
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

        this.settingsStorage.save(settings);
    }

    @Test
    public void shouldParseADFSMetadata() throws IOException, XPathException, CertificateEncodingException {
        var metadataFilePath = "src/test/resources/FederationMetadata.xml";
        var metadataXml = Files.readFile(Paths.get(metadataFilePath).toAbsolutePath().toFile());

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
        var metadataXml = Files.readFile(Paths.get(metadataFilePath).toAbsolutePath().toFile());

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

        HttpAuthenticationResult httpAuthenticationResult = simulateSAMLResponse(samlResponsePath, metadataFilePath, null, null);
        assertThat(httpAuthenticationResult.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));;
    }

    private HttpAuthenticationResult simulateSAMLResponse(String samlResponsePath, String metadataFilePath, String callbackUrl, String entityId) throws IOException, CertificateEncodingException, XPathException {
        var request = mock(HttpServletRequest.class);

        if (entityId == null) {
            entityId = "http://test.lan/app/callback";
        }

        if (metadataFilePath != null) {
            var metadataXml = Files.readFile(Paths.get(metadataFilePath).toAbsolutePath().toFile());

            SamlPluginSettings settings = new SamlPluginSettings();
            this.scheme.importMetadataIntoSettings(metadataXml, settings);

            settings.setEntityId(entityId);
            this.settingsStorage.save(settings);
        }

        if (callbackUrl == null) {
            callbackUrl = "http://test.lan/app/callback";
        }

        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        // built using https://capriza.github.io/samling/samling.html#samlPropertiesTab
        var saml = Files.readFile(Paths.get(samlResponsePath).toAbsolutePath().toFile());
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
