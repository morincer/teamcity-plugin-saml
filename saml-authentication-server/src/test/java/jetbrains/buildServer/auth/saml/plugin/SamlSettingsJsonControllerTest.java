package jetbrains.buildServer.auth.saml.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.auth.saml.plugin.pojo.MetadataImport;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamlSettingsJsonControllerTest {

    private SamlSettingsJsonController controller;
    private RootUrlHolder rootUrlHolder;
    private SamlPluginSettingsStorage settingsStorage;
    private WebControllerManager webControllerManager;
    private UserModel userModel;
    private UserGroupManager userGroupManager;
    private SamlAuthenticationScheme samlAuthenticationScheme;
    private SamlPluginPermissionsManager permissionManager;

    @Before
    public void setUp() throws Exception {
        Mockito.reset();
        this.rootUrlHolder = mock(RootUrlHolder.class);
        when(rootUrlHolder.getRootUrl()).thenReturn("http://my.url");

        this.webControllerManager = mock(WebControllerManager.class);

        this.settingsStorage = new InMemorySamlPluginSettingsStorage();

        this.userModel = mock(UserModel.class);
        this.userGroupManager = mock(UserGroupManager.class);

        LoginConfiguration loginConfiguration = mock(LoginConfiguration.class);
        samlAuthenticationScheme = new SamlAuthenticationScheme(rootUrlHolder, settingsStorage, userModel, userGroupManager, loginConfiguration);

        this.permissionManager = mock(SamlPluginPermissionsManager.class);
        when(this.permissionManager.hasPermission(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(true);

        controller = new SamlSettingsJsonController(this.samlAuthenticationScheme, this.settingsStorage, this.permissionManager, this.webControllerManager);
    }

    @Test
    public void shouldInitializeCallbackUrlWithRootUrl() {
        var request = mock(HttpServletRequest.class);

        var settings = this.controller.getSettings(request);
        var actualUrl = settings.getResult().getSettings().getSsoCallbackUrl();

        assertThat(actualUrl, org.hamcrest.core.StringStartsWith.startsWith("http://my.url"));
    }

    @Test
    public void shouldReturnCsrfToken() {
        var request = mock(HttpServletRequest.class);

        var session = mock(HttpSession.class);
        String token = "TOKEN";
        when(session.getAttribute("tc-csrf-token")).thenReturn(token);
        when(request.getSession()).thenReturn(session);

        var settings = this.controller.getSettings(request);
        assertThat(settings.getResult().getCsrfToken(), equalTo(token));
    }

    @Test
    public void shouldSetDefaultEntityIdToCallbackUrl() {
        var request = mock(HttpServletRequest.class);

        var settings = this.controller.getSettings(request);

        assertThat(settings, notNullValue());
        assertThat(settings.getErrors(), nullValue());
        assertThat(settings.getResult(), notNullValue());

        var enitityId = settings.getResult().getSettings().getEntityId();
        var callbackUrl = settings.getResult().getSettings().getSsoCallbackUrl();

        assertThat(callbackUrl, notNullValue());
        assertThat(enitityId, equalTo(callbackUrl));
    }

    @Test
    public void shouldParseMetadataIntoSettings() throws IOException {
        var metadataFilePath = "src/test/resources/metadata.xml";
        var metadataPubCert = "src/test/resources/metadata_pub.key";

        var metadataXml = FileUtils.readFileToString(Paths.get(metadataFilePath).toAbsolutePath().toFile());
        var metadataCert = FileUtils.readFileToString(Paths.get(metadataPubCert).toAbsolutePath().toFile());

        var metadataJson = new MetadataImport();
        metadataJson.setMetadataXml(metadataXml);

        var mapper = new ObjectMapper();
        var jsonString = mapper.writeValueAsString(metadataJson);

        var reader = new BufferedReader(new StringReader(jsonString));

        var request = mock(HttpServletRequest.class);
        when(request.getReader()).thenReturn(reader);

        assertThat(metadataXml, notNullValue());

        var result = this.controller.importMetadata(request);

        assertThat(result.getErrors(), nullValue());

        var settings = result.getResult();
        assertThat(settings, notNullValue());

        assertThat(settings.getIssuerUrl(), equalTo("entityid"));
        assertThat(settings.getSsoEndpoint(), equalTo("https://mycert.lan"));
        assertThat(settings.getPublicCertificate(), equalToIgnoringWhiteSpace(metadataCert));
    }

    @Test
    public void shouldErrorWhenMetadataEmpty() {
        var reader = new BufferedReader(new StringReader(""));

    }

    @Test
    public void shouldErrorWhenMetadataWrong() {

    }
}
