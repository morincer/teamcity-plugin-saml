package jetbrains.buildServer.auth.saml.plugin;

import com.onelogin.saml2.settings.Metadata;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.commons.validator.routines.RegexValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class SamlMetadataController extends BaseController {
    private final SamlAuthenticationScheme samlAuthenticationScheme;

    public SamlMetadataController(@NotNull SBuildServer server,
                                  @NotNull WebControllerManager webControllerManager,
                                  @NotNull AuthorizationInterceptor interceptor,
                                  @NotNull SamlAuthenticationScheme samlAuthenticationScheme,
                                  @NotNull SamlPluginSettingsStorage settingsStorage) {
        super(server);
        this.samlAuthenticationScheme = samlAuthenticationScheme;

        interceptor.addPathNotRequiringAuth(SamlPluginConstants.SAML_METADATA_URL);
        webControllerManager.registerController(SamlPluginConstants.SAML_METADATA_URL, this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        if (!this.samlAuthenticationScheme.isConfigured()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return simpleView("No SAML authentication scheme is configured on the server");
        }

        try {
            Metadata metadata = this.samlAuthenticationScheme.generateSPMetadata();
            if (metadata != null)
            {
                response.setContentType("text/xml");
                PrintWriter writer = response.getWriter();

                writer.print(metadata.getMetadataString());
                writer.flush();
                return null;
            } else {
                return simpleView("");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return simpleView(e.getMessage());
        }
    }
}
