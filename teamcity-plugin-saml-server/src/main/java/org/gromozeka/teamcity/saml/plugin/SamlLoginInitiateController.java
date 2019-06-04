package org.gromozeka.teamcity.saml.plugin;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.extern.log4j.Log4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SamlLoginInitiateController extends BaseController {

    public SamlLoginInitiateController(@NotNull SBuildServer server,
                                       @NotNull WebControllerManager webControllerManager,
                                       @NotNull AuthorizationInterceptor interceptor) {
        super(server);

        Loggers.SERVER.info("Initializing SAML controller");

        interceptor.addPathNotRequiringAuth(SamlPluginConstants.SAML_INITIATE_LOGIN_URL);
        webControllerManager.registerController(SamlPluginConstants.SAML_INITIATE_LOGIN_URL, this);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) throws Exception {
        httpServletResponse.sendRedirect("http://ya.ru");
        return new ModelAndView();
    }
}
