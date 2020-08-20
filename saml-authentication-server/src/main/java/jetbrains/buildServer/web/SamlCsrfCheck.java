package jetbrains.buildServer.web;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.auth.saml.plugin.SamlAuthenticationScheme;
import jetbrains.buildServer.auth.saml.plugin.SamlPluginConstants;
import jetbrains.buildServer.auth.saml.plugin.SamlPluginSettingsStorage;
import jetbrains.buildServer.log.Loggers;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;

public class SamlCsrfCheck implements CsrfCheck {

    private SamlAuthenticationScheme scheme;
    private SamlPluginSettingsStorage settingsStorage;
    private static final Logger LOG = Loggers.AUTH;

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

            Loggers.AUTH.debug("Evaluating SAML CORS filter conditions for " + request.getRequestURL());

            URL callbackUrl = scheme.getCallbackUrl();
            var requestURL = new URL(request.getRequestURL().toString());

            if (callbackUrl == null ) {
                Loggers.AUTH.debug("Callback URL is not set");
                return UNKNOWN;
            }

            if (!requestURL.toString().endsWith("/")) {
                requestURL = new URL(requestURL.toString() + "/");
            }

            if (!callbackUrl.toString().endsWith("/")) {
                callbackUrl = new URL(callbackUrl.toString() + "/");
            }

            if ("POST".equals(request.getMethod()) && callbackUrl.getPath().equals(requestURL.getPath())) {
                var parameter = request.getParameter(SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER);
                if (StringUtils.isEmpty(parameter)) {
                    LOG.debug("SAML CORS Check: " + SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER + " is not found in the request - responding with UNKNOWN result");
                    return UNKNOWN;
                }

                LOG.info(String.format("CSRF is marked safe via SAML callback target for %s", request.getRequestURL()));

                return CheckResult.safe();
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return UNKNOWN;
    }

    @NotNull
    @Override
    public String describe(boolean b) {
        return "SAML login requests to SSO callback URL are CORS-safe";
    }
}
