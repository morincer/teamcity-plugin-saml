package jetbrains.buildServer.auth.saml.plugin;

import com.intellij.openapi.diagnostic.Logger;
import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlAttributeMappingSettings;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationSchemeAdapter;
import jetbrains.buildServer.controllers.interceptors.auth.util.HttpAuthUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.auth.ServerPrincipal;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.util.StringUtil;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SamlAuthenticationScheme extends HttpAuthenticationSchemeAdapter {

    private final Logger LOG = Loggers.SERVER;
    private RootUrlHolder rootUrlHolder;
    private SamlPluginSettingsStorage settingsStorage;
    private UserModel userModel;


    public SamlAuthenticationScheme(
            @NotNull RootUrlHolder rootUrlHolder,
            @NotNull final SamlPluginSettingsStorage settingsStorage,
            @NotNull UserModel userModel) {
        this.rootUrlHolder = rootUrlHolder;
        this.settingsStorage = settingsStorage;
        this.userModel = userModel;
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

    public void sendAuthnRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException, SettingsException {
        var samlSettings = buildSettings();
        var auth = new Auth(samlSettings, request, response);
        auth.login();
    }

    @NotNull
    @Override
    public HttpAuthenticationResult processAuthenticationRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Map<String, String> properties) throws IOException {
        var saml = request.getParameter(SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER);
        if (StringUtil.isEmpty(saml)) {
            return HttpAuthenticationResult.notApplicable();
        }
        try {
            var settings = this.settingsStorage.load();

            var saml2Settings = buildSettings();
            var auth = new Auth(saml2Settings, request, response);

            auth.processResponse();

            if (!auth.isAuthenticated()) {
                return sendUnauthorizedRequest(request, response, String.format("SAML request is not authenticated due to errors: " + String.join(", ", auth.getErrors())));
            }

            String username = auth.getNameId();

            SUser user = null;

            if (StringUtils.isEmpty(username)) {
                LOG.error("Username is empty - authentication stops");
            } else {
                user = userModel.findUserAccount(null, username);

                if (user == null) {
                    user = userModel.findUserByUsername(username, SamlPluginConstants.ID_USER_PROPERTY_KEY);
                }

                if (user == null && settings.isCreateUsersAutomatically()) {
                    try {
                        if (!settings.isLimitToPostfixes() || matchPostfixes(username, settings.getAllowedPostfixes())) {
                            LOG.info(String.format("Creating new user %s from SAML request", username));
                            user = userModel.createUserAccount(null, username);

                            String email = getAttribute(auth, settings.getEmailAttributeMapping());
                            String fullname = getAttribute(auth, settings.getNameAttributeMapping());

                            LOG.info(String.format("Setting data for new user: username=%s, full name=%s, email=%s", username, fullname, email));

                            user.updateUserAccount(username, fullname, email);

                            if (user == null) {
                                LOG.warn(String.format("New user %s was not created due to unknown reason", username));
                            }
                        }
                    } catch (Exception e) {
                        LOG.error(String.format("Failed to create new user with username %s: %s", username, e.getMessage()), e);
                    }
                }
            }

            if (user == null) {
                return sendUnauthorizedRequest(request, response, String.format("SAML request NOT authenticated for user id %s: user with such username or %s property value not found", username, SamlPluginConstants.ID_USER_PROPERTY_KEY));
            }

            LOG.info(String.format("SAML request authenticated for user %s/%s", user.getUsername(), user.getName()));

            return HttpAuthenticationResult.authenticated(
                    new ServerPrincipal(user.getRealm(), user.getUsername(), null, settings.isCreateUsersAutomatically(), new HashMap<>()),
                    true).withRedirect("/");
        } catch (Exception e) {
            LOG.error(e);
            return sendUnauthorizedRequest(request, response, String.format("Failed to authenticate request: %s", e.getMessage()));
        }
    }

    @NotNull
    private String getAttribute(@NotNull Auth saml, @NotNull SamlAttributeMappingSettings attributeMappingSettings) {
        switch (attributeMappingSettings.getMappingType()) {
            case SamlAttributeMappingSettings.TYPE_NONE: return "";
            case SamlAttributeMappingSettings.TYPE_NAME_ID: return saml.getNameId();
            case SamlAttributeMappingSettings.TYPE_OTHER:
                if (StringUtil.isEmpty(attributeMappingSettings.getCustomAttributeName())) {
                    LOG.warn("Custom attribute name is not set");
                    return "";
                }

                var attributeValue = saml.getAttribute(attributeMappingSettings.getCustomAttributeName());
                if (attributeValue.size() == 0) return "";

                var result = attributeValue.stream().collect(Collectors.joining(", "));
                return result;
            default:
                LOG.warn(String.format("Unknow mapping type: %s", attributeMappingSettings.getMappingType()));
                return "";
        }
    }

    private boolean matchPostfixes(String username, String allowedPostfixes) {
        var postfixes = allowedPostfixes.split(",");
        for(var postfix : postfixes) {
            if (username.trim().endsWith(postfix.trim())) {
                LOG.info(String.format("Username %s ends with valid postfix %s", username, postfix));
                return true;
            }
        }

        LOG.warn(String.format("No valid postfixes were detected for username %s", username));
        return false;
    }

    private HttpAuthenticationResult sendUnauthorizedRequest(HttpServletRequest request, HttpServletResponse response, String reason) throws IOException {
        LOG.warn(reason);
        HttpAuthUtil.setUnauthenticatedReason(request, reason);
        response.sendError(HttpStatus.UNAUTHORIZED.value(), reason);
        return HttpAuthenticationResult.unauthenticated();
    }

    public URL getCallbackUrl() throws MalformedURLException {
        return new URL(new URL(rootUrlHolder.getRootUrl()), SamlPluginConstants.SAML_CALLBACK_URL.replace("**", ""));
    }

    private Saml2Settings buildSettings() throws IOException {
        var pluginSettings = settingsStorage.load();

        Map<String, Object> samlData = new HashMap<>();
        samlData.put(SettingsBuilder.IDP_SINGLE_SIGN_ON_SERVICE_URL_PROPERTY_KEY, pluginSettings.getSsoEndpoint());
        samlData.put(SettingsBuilder.IDP_ENTITYID_PROPERTY_KEY, pluginSettings.getIssuerUrl());
        samlData.put(SettingsBuilder.SP_ENTITYID_PROPERTY_KEY, pluginSettings.getEntityId());
        samlData.put(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_URL_PROPERTY_KEY, getCallbackUrl());
        samlData.put(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, pluginSettings.getPublicCertificate());
        samlData.put(SettingsBuilder.COMPRESS_REQUEST, pluginSettings.isCompressRequest());

        var builder = new SettingsBuilder();
        var samlSettings = builder.fromValues(samlData).build();

        var errors = samlSettings.checkSettings();

        if (!errors.isEmpty()) {
            throw new IOException(String.format("Failed to configure SAML processor: %s", String.join(", ", errors)));
        }

        return samlSettings;
    }


}
