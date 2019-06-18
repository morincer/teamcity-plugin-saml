package org.gromozeka.teamcity.saml.plugin;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.gromozeka.teamcity.saml.core.config.SamlPluginSettingsStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SamlLoginCallbackController extends BaseController {

    private final SamlPluginSettingsStorage settingsStorage;

    public SamlLoginCallbackController(@NotNull SBuildServer server,
                                       @NotNull WebControllerManager webControllerManager,
                                       @NotNull AuthorizationInterceptor interceptor,
                                       @NotNull SamlPluginSettingsStorage settingsStorage) {
        super(server);
        this.settingsStorage = settingsStorage;

        Loggers.SERVER.info("Initializing SAML callback controller");

//        interceptor.addPathNotRequiringAuth(SamlPluginConstants.SAML_CALLBACK_URL);
        webControllerManager.registerController(SamlPluginConstants.SAML_CALLBACK_URL, this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        return new ModelAndView(new RedirectView("/"));
    }
}
