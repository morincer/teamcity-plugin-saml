package org.gromozeka.teamcity.saml.plugin;

import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationScheme;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationSchemeAdapter;
import jetbrains.buildServer.controllers.interceptors.auth.util.HttpAuthUtil;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class SamlAuthenticationScheme extends HttpAuthenticationSchemeAdapter {

    public SamlAuthenticationScheme(final LoginConfiguration loginConfiguration) {
        loginConfiguration.registerAuthModuleType(this);
    }

    @NotNull
    @Override
    protected String doGetName() {
        return "SAML-based Authentication";
    }
}