package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.testng.reporters.Files;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import java.nio.file.Paths;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

public class SamlLoginControllerTest {

    private SamlLoginController controller;
    private WebControllerManager webControllerManager;
    private InMemorySamlPluginSettingsStorage settingsStorage;
    private SBuildServer server;
    private AuthorizationInterceptor interceptor;
    private static String ssoEndpoint = "http://nowhere.com";
    private UserModel userModel;
    private SamlAuthenticationScheme scheme;

    @Before
    public void setUp() throws Exception {
        Mockito.reset();
        this.webControllerManager = mock(WebControllerManager.class);
        this.settingsStorage = new InMemorySamlPluginSettingsStorage();
        this.settingsStorage.settings.setSsoEndpoint(ssoEndpoint);


        this.server = mock(SBuildServer.class);
        this.interceptor = mock(AuthorizationInterceptor.class);
        this.userModel = mock(UserModel.class);

        scheme = new SamlAuthenticationScheme(settingsStorage, userModel);
        controller = new SamlLoginController(this.server, this.webControllerManager, this.interceptor, this.scheme, this.settingsStorage);
    }

    @Test
    public void shouldRedirectInDedicatedSsoUrlMode() throws Exception {
        this.settingsStorage.settings.setDedicatedSsoUrlMode(true);

        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);

        ModelAndView result = controller.doHandle(request, response);
        RedirectView redirectView = (RedirectView) result.getView();
        assertThat(redirectView, is(notNullValue()));
        assertThat(redirectView.getUrl(), equalTo(ssoEndpoint));
    }

    @Test
    public void shouldSendAuthnRequestInNonDedicatedSsoUrlMode() throws Exception {
        this.settingsStorage.settings.setDedicatedSsoUrlMode(false);
        this.settingsStorage.settings.setEntityId("http://IdP.com");
        this.settingsStorage.settings.setIssuerUrl("http://nowhere.com");
        this.settingsStorage.settings.setPublicCertificate(Files.readFile(Paths.get("src/test/resources/metadata_pub.key").toFile()));

        var request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer(ssoEndpoint));

        var response = new MockHttpServletResponse();

        ModelAndView result = controller.doHandle(request, response);
        assertThat(result, is(nullValue()));
        assertThat(response.getHeader("Location"), equalTo(ssoEndpoint));
    }
}
