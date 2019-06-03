package org.gromozeka.teamcity.saml.plugin;

import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationScheme;
import jetbrains.buildServer.controllers.interceptors.auth.util.HttpAuthUtil;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class SamlAuthenticationScheme implements HttpAuthenticationScheme {
    @NotNull
    @Override
    public HttpAuthenticationResult processAuthenticationRequest(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Map<String, String> map) throws IOException {
        return HttpAuthenticationResult.unauthenticated();
    }

    @NotNull
    @Override
    public String getName() {
        return "SAML Authentication";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "SAML AuthenticationScheme";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Authenticates users basing on SAML assertions";
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }

    @NotNull
    @Override
    public Map<String, String> getDefaultProperties() {
        return null;
    }

    @Nullable
    @Override
    public String getEditPropertiesJspFilePath() {
        return "/buildServerResources/SamlConfigure.jsp";
    }

    @NotNull
    @Override
    public String describeProperties(@NotNull Map<String, String> map) {
        return null;
    }

    @Nullable
    @Override
    public Collection<String> validate(@NotNull Map<String, String> map) {
        return null;
    }
}
