package jetbrains.buildServer.auth.saml.plugin;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.apache.commons.validator.UrlValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SamlLoginController extends BaseController {

    private final SamlPluginSettingsStorage settingsStorage;

    private final Logger LOG = Loggers.SERVER;

    public SamlLoginController(@NotNull SBuildServer server,
                               @NotNull WebControllerManager webControllerManager,
                               @NotNull AuthorizationInterceptor interceptor,
                               @NotNull SamlPluginSettingsStorage settingsStorage) {
        super(server);
        this.settingsStorage = settingsStorage;

        LOG.info("Initializing SAML controller");

        interceptor.addPathNotRequiringAuth(SamlPluginConstants.SAML_INITIATE_LOGIN_URL);
        webControllerManager.registerController(SamlPluginConstants.SAML_INITIATE_LOGIN_URL, this);
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

            var urlValidator = new UrlValidator();

            if (!urlValidator.isValid(endpoint)) throw new Exception(String.format("SSO endpoint (%s) must be a valid URL ", endpoint));

            LOG.info(String.format("Redirecting to %s", endpoint));
            return new ModelAndView(new RedirectView(endpoint));

        } catch (Exception e) {
            LOG.error(String.format("Error while initating SSO login redirect: ", e.getMessage()), e);
            throw e;
        }
    }
}
