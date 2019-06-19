package org.gromozeka.teamcity.saml.plugin;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationSchemeAdapter;
import jetbrains.buildServer.controllers.interceptors.auth.util.HttpAuthUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.serverSide.auth.ServerPrincipal;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SamlAuthenticationScheme extends HttpAuthenticationSchemeAdapter {

    private PluginDescriptor pluginDescriptor;
    private SamlPluginSettingsStorage settingsStorage;
    private UserModel userModel;

    public SamlAuthenticationScheme(
            @NotNull PluginDescriptor pluginDescriptor,
            final LoginConfiguration loginConfiguration,
            @NotNull final SamlPluginSettingsStorage settingsStorage,
            @NotNull UserModel userModel) {
        this.pluginDescriptor = pluginDescriptor;

        this.settingsStorage = settingsStorage;
        this.userModel = userModel;
        loginConfiguration.registerAuthModuleType(this);
        Loggers.SERVER.info("Registered SAML-based authentication scheme");
    }

    @NotNull
    @Override
    protected String doGetName() {
        return SamlPluginConstants.AUTH_SCHEME_NAME;
    }

    @NotNull
    @Override
    public String getDescription() {
        return SamlPluginConstants.AUTH_SCHEME_DESCRIPTION;
    }

    @Override
    public boolean isMultipleInstancesAllowed() {
        return false;
    }

    @NotNull
    @Override
    public HttpAuthenticationResult processAuthenticationRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Map<String, String> properties) throws IOException {
        var saml = request.getParameter("SAMLResponse");
        if (StringUtil.isEmpty(saml)) {
            return HttpAuthenticationResult.notApplicable();
        }
        try {
            var settings = buildSettings(new URL(request.getRequestURL().toString()));
            var auth = new Auth(settings, request, response);
            auth.processResponse();

            if (!auth.isAuthenticated()) {
                return sendUnauthorizedRequest(request, response, String.format("SAML request is not authenticated due to errors: " + String.join(", ", auth.getErrors())));
            }

            var user = userModel.findUserAccount(null, auth.getNameId());

            if (user == null) {
                user = userModel.findUserByUsername(auth.getNameId(), SamlPluginConstants.ID_USER_PROPERTY_KEY);
            }

            if (user == null) {
                return sendUnauthorizedRequest(request, response, String.format("SAML request NOT authenticated for user id %s: user with such username or %s property value not found", auth.getNameId(), SamlPluginConstants.ID_USER_PROPERTY_KEY));
            }

            Loggers.SERVER.info(String.format("SAML request authenticated for user %s", user.getName()));

            return HttpAuthenticationResult.authenticated(
                    new ServerPrincipal(user.getRealm(), user.getUsername(), null, false, new HashMap<>()),
                    true).withRedirect("/");
        } catch (Exception e) {
            Loggers.SERVER.error(e);
            return sendUnauthorizedRequest(request, response, String.format("Failed to authenticate request: %s", e.getMessage()));
        }
    }

    private HttpAuthenticationResult sendUnauthorizedRequest(HttpServletRequest request, HttpServletResponse response, String reason) throws IOException {
        Loggers.SERVER.warn(reason);
        HttpAuthUtil.setUnauthenticatedReason(request, reason);
        response.sendError(HttpStatus.UNAUTHORIZED.value(), reason);
        return HttpAuthenticationResult.unauthenticated();
    }

    private Saml2Settings buildSettings(URL baseUrl) throws IOException {
        var pluginSettings = settingsStorage.load();

        Map<String, Object> samlData = new HashMap<>();
        samlData.put(SettingsBuilder.IDP_SINGLE_SIGN_ON_SERVICE_URL_PROPERTY_KEY, pluginSettings.getSsoEndpoint());
        samlData.put(SettingsBuilder.IDP_ENTITYID_PROPERTY_KEY, pluginSettings.getIssuerUrl());
        samlData.put(SettingsBuilder.SP_ENTITYID_PROPERTY_KEY, pluginSettings.getEntityId());
        samlData.put(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_URL_PROPERTY_KEY,
                new URL(baseUrl, SamlPluginConstants.SAML_CALLBACK_URL.replace("**", "")));
        samlData.put(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, pluginSettings.getPublicCertificate());

        var builder = new SettingsBuilder();
        var samlSettings = builder.fromValues(samlData).build();

        var errors = samlSettings.checkSettings();

        if (!errors.isEmpty()) {
            throw new IOException(String.format("Failed to configure SAML processor: %s", String.join(", ", errors)));
        }

        return samlSettings;
    }


}