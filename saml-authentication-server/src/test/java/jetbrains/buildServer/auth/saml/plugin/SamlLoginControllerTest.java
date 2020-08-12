package jetbrains.buildServer.auth.saml.plugin;

import com.onelogin.saml2.util.Util;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamlLoginControllerTest {

    private SamlLoginController controller;
    private WebControllerManager webControllerManager;
    private InMemorySamlPluginSettingsStorage settingsStorage;
    private SBuildServer server;
    private AuthorizationInterceptor interceptor;
    private static String ssoEndpoint = "http://nowhere.com";
    private UserModel userModel;
    private UserGroupManager userGroupManager;
    private SamlAuthenticationScheme scheme;

    @Before
    public void setUp() throws Exception {
        Mockito.reset();
        this.webControllerManager = mock(WebControllerManager.class);
        this.settingsStorage = new InMemorySamlPluginSettingsStorage();
        this.settingsStorage.settings.setSsoEndpoint(ssoEndpoint);

        this.server = mock(SBuildServer.class);
        when(this.server.getRootUrl()).thenReturn("http://server.com");
        this.interceptor = mock(AuthorizationInterceptor.class);
        this.userModel = mock(UserModel.class);
        this.userGroupManager = mock(UserGroupManager.class);

        LoginConfiguration loginConfiguration = mock(LoginConfiguration.class);

        scheme = new SamlAuthenticationScheme(server, settingsStorage, userModel, userGroupManager, loginConfiguration);
        controller = new SamlLoginController(this.server, this.webControllerManager, this.interceptor, this.scheme, this.settingsStorage);
    }

    @Test
    public void shouldSendAuthnRequest() throws Exception {
        this.settingsStorage.settings.setEntityId("http://IdP.com");
        this.settingsStorage.settings.setIssuerUrl("http://nowhere.com");
        this.settingsStorage.settings.setPublicCertificate(FileUtils.readFileToString(Paths.get("src/test/resources/metadata_pub.key").toFile()));

        var request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer(ssoEndpoint));

        var response = new MockHttpServletResponse();

        ModelAndView result = controller.doHandle(request, response);
        assertThat(result, is(nullValue()));
        String redirectedUrl = response.getRedirectedUrl();
        assertThat(redirectedUrl, not(isEmptyString()));
        URL url = new URL(redirectedUrl);
        assertThat(url.getHost(), equalTo(new URL(ssoEndpoint).getHost()));
        assertThat(url.getQuery(), containsString("SAMLRequest"));
    }

    @Test
    public void validateUrl() {
        String url = "http://keycloak.lan:8080/auth/realms/master/protocol/saml";
        assertThat(controller.validateUrl(url), is(true));

        url = "http://do-openam.localnet.local:8080/openam/";
        assertThat(controller.validateUrl(url), is(true));
    }

    @Test
    public void shouldUseBaseUrlFromServerConfiguration() throws Exception {
        this.settingsStorage.settings.setEntityId("http://IdP.com");
        this.settingsStorage.settings.setIssuerUrl("http://nowhere.com");
        this.settingsStorage.settings.setPublicCertificate(FileUtils.readFileToString(Paths.get("src/test/resources/metadata_pub.key").toFile()));
        this.settingsStorage.settings.getSsoCallbackUrl();
        this.settingsStorage.settings.setCompressRequest(false);

        var request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer(ssoEndpoint));
        when(server.getRootUrl()).thenReturn("https://server.com");

        var response = new MockHttpServletResponse();

        ModelAndView result = controller.doHandle(request, response);
        assertThat(result, is(nullValue()));
        String redirectedUrl = response.getRedirectedUrl();
        assertThat(redirectedUrl, not(isEmptyString()));

        URL url = new URL(redirectedUrl);
        String samlRequest = Arrays.stream(url.getQuery()
                .split("&"))
                .filter(s -> s.startsWith("SAMLRequest="))
                .map(s -> s.replace("SAMLRequest=", ""))
                .map(s -> new String(Util.base64decoder(s))).collect(Collectors.joining());

        assertThat(samlRequest, containsString("AssertionConsumerServiceURL=\"https://server.com/app/saml/callback/\""));
        
    }
}
