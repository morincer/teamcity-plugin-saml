package jetbrains.buildServer.web;

import jetbrains.buildServer.auth.saml.plugin.SamlAuthenticationScheme;
import jetbrains.buildServer.auth.saml.plugin.SamlPluginConstants;
import jetbrains.buildServer.auth.saml.plugin.SamlPluginSettingsStorage;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;

public class SamlCsrfCheck implements CsrfCheck {

    private SamlAuthenticationScheme scheme;
    private SamlPluginSettingsStorage settingsStorage;

    public SamlCsrfCheck(SamlAuthenticationScheme scheme, SamlPluginSettingsStorage settingsStorage) {
        this.scheme = scheme;
        this.settingsStorage = settingsStorage;
    }

    @Override
    public CheckResult isSafe(@NotNull HttpServletRequest request) {

        if (!scheme.isConfigured()) return UNKNOWN;

        try {
            if (!this.settingsStorage.load().isSamlCorsFilter()) {
                Loggers.AUTH.debug("SAML CORS filter is disabled by plugin configuration - skipping");
                return UNKNOWN;
            }

            Loggers.AUTH.debug("Evaluating SAML CORS filter conditions");

            URL callbackUrl = scheme.getCallbackUrl();
            var requestURL = new URL(request.getRequestURL().toString());

            if (callbackUrl == null) return UNKNOWN;

            if ("POST".equals(request.getMethod()) && callbackUrl.getPath().equals(requestURL.getPath())) {
                var parameter = request.getParameter(SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER);
                if (StringUtils.isEmpty(parameter)) {
                    Loggers.AUTH.debug("SAML CORS Check: " + SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER + " is not found in the request - responding with UNKNOWN result");
                    return UNKNOWN;
                }

                Loggers.AUTH.debug("CSRF validated via SAML callback target");

                return CheckResult.safe();
            }

        } catch (Exception e) {
            Loggers.AUTH.error(e.getMessage(), e);
        }

        return UNKNOWN;
    }

    @NotNull
    @Override
    public String describe(boolean b) {
        return "SAML login requests to SSO callback URL are CORS-safe";
    }
}
