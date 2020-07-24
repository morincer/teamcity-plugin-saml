package jetbrains.buildServer.web;

import jetbrains.buildServer.auth.saml.plugin.InMemorySamlPluginSettingsStorage;
import jetbrains.buildServer.auth.saml.plugin.SamlAuthenticationScheme;
import jetbrains.buildServer.auth.saml.plugin.SamlPluginConstants;
import jetbrains.buildServer.serverSide.auth.AuthModule;
import jetbrains.buildServer.serverSide.auth.AuthModuleType;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import lombok.var;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamlCsrfCheckTest {

    private SamlCsrfCheck check;
    private SamlAuthenticationScheme scheme;

    @Before
    public void setUp() throws Exception {
        Mockito.reset();
        this.scheme = mock(SamlAuthenticationScheme.class);
        when(this.scheme.isConfigured()).thenReturn(true);
        when(this.scheme.getCallbackUrl()).thenReturn(new URL("http://someurl.local"));

        InMemorySamlPluginSettingsStorage settingsStorage = new InMemorySamlPluginSettingsStorage();

        this.check = new SamlCsrfCheck(this.scheme, settingsStorage);
    }

    @Test
    public void whenSAMLRequestIsMadeToCallbackURLItIsSafe() throws MalformedURLException {
        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        StringBuffer URL = new StringBuffer(this.scheme.getCallbackUrl().toString());
        when(request.getRequestURL()).thenReturn(URL);
        when(request.getParameter(SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER)).thenReturn("SAMLResponse=1");

        CsrfCheck.CheckResult result = this.check.isSafe(request);

        assertThat(result.isSafe(), equalTo(true));
    }

    @Test
    public void shouldNotFireWhenSchemeIsDisabled() {
        when(this.scheme.isConfigured()).thenReturn(false);
        var request = mock(HttpServletRequest.class);

        assertThat(this.check.isSafe(request).isSafe(), equalTo(false));
    }
}
