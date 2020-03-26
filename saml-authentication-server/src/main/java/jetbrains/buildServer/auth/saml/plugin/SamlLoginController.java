package jetbrains.buildServer.auth.saml.plugin;

import com.intellij.openapi.diagnostic.Logger;
import com.onelogin.saml2.Auth;
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SamlLoginController extends BaseController {

    private final RegexValidator anyAuthorityValidator;
    private SamlAuthenticationScheme samlAuthenticationScheme;
    private final SamlPluginSettingsStorage settingsStorage;

    private final Logger LOG = Loggers.SERVER;

    public SamlLoginController(@NotNull SBuildServer server,
                               @NotNull WebControllerManager webControllerManager,
                               @NotNull AuthorizationInterceptor interceptor,
                               @NotNull SamlAuthenticationScheme samlAuthenticationScheme,
                               @NotNull SamlPluginSettingsStorage settingsStorage) {
        super(server);
        this.samlAuthenticationScheme = samlAuthenticationScheme;
        this.settingsStorage = settingsStorage;
        this.anyAuthorityValidator = new RegexValidator(".*");

        LOG.info("Initializing SAML controller");

        interceptor.addPathNotRequiringAuth(SamlPluginConstants.SAML_INITIATE_LOGIN_URL);
        webControllerManager.registerController(SamlPluginConstants.SAML_INITIATE_LOGIN_URL, this);
    }

    public boolean validateUrl(String url) {
        return new UrlValidator(anyAuthorityValidator, UrlValidator.ALLOW_ALL_SCHEMES + UrlValidator.ALLOW_LOCAL_URLS).isValid(url);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) throws Exception {

        try {
            LOG.info("Initiating SSO login");

            var settings = settingsStorage.load();

            var endpoint = settings.getSsoEndpoint();

            if (endpoint == null || "".equals(endpoint.trim())) {
                throw new Exception("You must configure a valid SSO endpoint");
            }

            boolean ssoEndpointIsValid = validateUrl(endpoint);
            if (!ssoEndpointIsValid) {
                throw new Exception(String.format("SSO endpoint (%s) must be a valid URL ", endpoint));
            }

            LOG.info(String.format("Building AuthNRequest to %s", endpoint));
            this.samlAuthenticationScheme.sendAuthnRequest(httpServletRequest, httpServletResponse);
            return null;
        } catch (Exception e) {
            LOG.error(String.format("Error while initating SSO login redirect: ", e.getMessage()), e);
            throw e;
        }
    }
}
