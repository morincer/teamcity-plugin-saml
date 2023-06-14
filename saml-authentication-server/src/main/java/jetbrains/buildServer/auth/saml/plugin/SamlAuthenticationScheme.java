package jetbrains.buildServer.auth.saml.plugin;

import com.intellij.openapi.diagnostic.Logger;
import com.onelogin.saml2.Auth;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.settings.IdPMetadataParser;
import com.onelogin.saml2.settings.Metadata;
import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;
import com.onelogin.saml2.util.Util;
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlAttributeMappingSettings;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import jetbrains.buildServer.auth.saml.plugin.utils.SpelExpressionContext;
import jetbrains.buildServer.auth.saml.plugin.utils.SpelExpressionExecutor;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationSchemeAdapter;
import jetbrains.buildServer.controllers.interceptors.auth.util.HttpAuthUtil;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.auth.AuthModuleType;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.serverSide.auth.ServerPrincipal;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.users.impl.UserEx;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.util.WebUtil;
import lombok.var;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.xpath.XPathException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SamlAuthenticationScheme extends HttpAuthenticationSchemeAdapter {

    private final Logger LOG = Loggers.AUTH;

    private final RootUrlHolder rootUrlHolder;
    private final SamlPluginSettingsStorage settingsStorage;
    private final UserModel userModel;
    private final UserGroupManager userGroupManager;
    private final LoginConfiguration loginConfiguration;


    public SamlAuthenticationScheme(
            @NotNull RootUrlHolder rootUrlHolder,
            @NotNull final SamlPluginSettingsStorage settingsStorage,
            @NotNull UserModel userModel,
            @NotNull UserGroupManager userGroupManager,
            @NotNull LoginConfiguration loginConfiguration) {

        this.rootUrlHolder = rootUrlHolder;
        this.settingsStorage = settingsStorage;
        this.userModel = userModel;
        this.userGroupManager = userGroupManager;
        this.loginConfiguration = loginConfiguration;
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
        if (request.getSession() != null) {
            Object urlKey = request.getSession().getAttribute("URL_KEY");
            if (urlKey instanceof String) {
                auth.login((String)urlKey);
                return;
            }
        }
        auth.login();
    }

    public Metadata generateSPMetadata() throws IOException, CertificateEncodingException {
        var saml2Settings = buildSettings();
        var metadata = new Metadata(saml2Settings);

        LOG.debug(String.format("SAML: SP Metadata generated %s", metadata.getMetadataString()));

        return metadata;
    }

    @NotNull
    @Override
    public HttpAuthenticationResult processAuthenticationRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Map<String, String> properties) throws IOException {
        LOG.debug(String.format("SAML: incoming authentication request %s %s",request.getMethod(), request.getRequestURL()));

        var saml = request.getParameter(SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER);
        var relayState = request.getParameter("RelayState");

        if (StringUtil.isEmpty(saml)) {
            LOG.debug(String.format("%s parameter not found - returning N/A", SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER));
            return HttpAuthenticationResult.notApplicable();
        }

        try {
            var settings = this.settingsStorage.load();

            var saml2Settings = buildSettings();
            var auth = new Auth(saml2Settings, request, response);

            auth.processResponse();

            if (!auth.isAuthenticated()) {
                return sendUnauthorizedRequest(request, response, "SAML request is not authenticated due to errors: " + String.join(", ", auth.getErrors()));
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

                            if (user == null) {
                                LOG.warn(String.format("New user %s was not created due to unknown reason", username));
                            } else {
                                String email = getAttribute(auth, settings.getEmailAttributeMapping());
                                String fullname = getAttribute(auth, settings.getNameAttributeMapping());
                                String vcsUsername = getAttribute(auth, settings.getVcsUsernameAttributeMapping());

                                LOG.info(String.format("Setting data for new user: username=%s, full name=%s, email=%s", username, fullname, email));

                                user.updateUserAccount(username, fullname, email);
                                if (StringUtil.isNotEmpty(vcsUsername)) {
                                    ((UserEx)user).setDefaultVcsUsernames(Collections.singletonList(vcsUsername));
                                }
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

            if (settings.isAssignGroups()) {
                String samlGroups = getAttribute(auth, settings.getGroupsAttributeMapping());
                LOG.debug(String.format("SAML Groups = '%s'", samlGroups));

                // Process the SAML groups assigned to this user
                processGroups(user, samlGroups, settings.isRemoveUnassignedGroups());
           }

            LOG.info(String.format("SAML request authenticated for user %s/%s", user.getUsername(), user.getName()));

            return authenticated(request, settings, user, relayState);
        } catch (Exception e) {
            LOG.error(e);
            return sendUnauthorizedRequest(request, response, String.format("Failed to authenticate request: %s", e.getMessage()));
        }
    }

    private static String getRedirectUrl(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if (session == null) {
            return request.getContextPath() + "/";
        }
        String url = (String) session.getAttribute("URL_KEY");
        session.removeAttribute("URL_KEY");
        return url != null ? url : request.getContextPath() + "/";
    }

    @NotNull
    private static HttpAuthenticationResult authenticated(@NotNull HttpServletRequest request, SamlPluginSettings settings, SUser user, String relayState) {
        return HttpAuthenticationResult.authenticated(
                new ServerPrincipal(user.getRealm(), user.getUsername(), null, settings.isCreateUsersAutomatically(), new HashMap<>()),
                true).withRedirect(relayState != null ? relayState : getRedirectUrl(request));
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

                String attributeName = attributeMappingSettings.getCustomAttributeName();
                var attributeValue = saml.getAttribute(attributeName);
                if (attributeValue == null) {
                    LOG.warn(String.format("Attribute '%s' not found in SAML response", attributeName));
                    return "";
                }
                if (attributeValue.size() == 0) return "";

                return String.join(", ", attributeValue);
            case SamlAttributeMappingSettings.TYPE_EXPRESSION:
                if (StringUtil.isEmpty(attributeMappingSettings.getCustomAttributeName())) {
                    LOG.warn("Expression is not set");
                    return "";
                }
                var expression = attributeMappingSettings.getCustomAttributeName();
                var executor = new SpelExpressionExecutor();
                var context = new SpelExpressionContext(saml);
                try {
                    String result = executor.evaluate(expression, context);
                    if (StringUtils.isEmpty(result)) {
                        LOG.warn(String.format("Expression %s evaluated to empty value.", expression));
                    }
                    return result;
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    LOG.warn(String.format("Available properties are: %s", context.getRootObjectAsMap().keySet().stream().collect(Collectors.joining(", "))));
                    return "";
                }
            default:
                LOG.warn(String.format("Unknown mapping type: %s", attributeMappingSettings.getMappingType()));
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

    private void processGroups(@NotNull SUser user, String groups, boolean removeUnassignedGroups) {
        if (groups == null) groups = "";

        // Get a Map of TeamCity groups, keyed by lowercase group Key
        var teamcityGroups = userGroupManager.getUserGroups().stream()
                .collect(Collectors.toMap(g -> g.getKey().toLowerCase(),
                        Function.identity()));

        // Get a lower-cased list of users current groups
        List<String> usersCurrentGroups = user.getUserGroups().stream()
                .filter(g -> !g.getKey().equals("ALL_USERS_GROUP")) // We don't want to remove the 'ALL_USERS_GROUP'
                .map(g -> g.getKey().toLowerCase())
                .collect(Collectors.toList());
        LOG.debug(String.format("Users current groups = '%s'", usersCurrentGroups));

        // Split the 'groups' string, lowercase and trim empty results
        List<String> usersAssignedGroups = Arrays.stream(groups.split(", "))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(StringUtil::isNotEmpty)
                .map(s -> "mlad bla-bla-bla".equals(s) ? "maplarge_admins" : s)
                .map(s -> "mlad bla-bla-bla another".equals(s) ? "maplarge_devs" : s)
                .collect(Collectors.toList());
        LOG.debug(String.format("Users assigned groups from SAML response: '%s'", usersAssignedGroups));

        // What groups to add and what groups to remove
        List<String> groupsToAdd = new ArrayList<>(CollectionUtils.subtract(usersAssignedGroups, usersCurrentGroups));

        // Add any new groups
        groupsToAdd.forEach(addGroup -> {
           if (teamcityGroups.containsKey(addGroup)) {
               LOG.info(String.format("Adding user to group '%s'", addGroup));
               teamcityGroups.get(addGroup).addUser(user);
           } else {
               LOG.info(String.format("No matching TeamCity group found for '%s'", addGroup));
           }
        });

        // Optionally remove groups that are no longer assigned in SAML response.
        if (removeUnassignedGroups) {
            List<String> groupsToRemove = new ArrayList<>(CollectionUtils.subtract(usersCurrentGroups, usersAssignedGroups));

            // Remove any groups that are no longer mapped
            groupsToRemove.forEach(removeGroup -> {
                if (teamcityGroups.containsKey(removeGroup)) {
                    LOG.info(String.format("Group '%s' has been unassigned from user. Removing...", removeGroup));
                    teamcityGroups.get(removeGroup).removeUser(user);
                } else {
                    LOG.warn(String.format("Existing mapped TeamCity group not found to remove: '%s'", removeGroup));
                }
            });
        }
    }

    private HttpAuthenticationResult sendUnauthorizedRequest(HttpServletRequest request, HttpServletResponse response, String reason) throws IOException {
        LOG.warn(reason);
        HttpAuthUtil.setUnauthenticatedReason(request, reason);
        response.sendError(HttpStatus.UNAUTHORIZED.value(), reason);
        return HttpAuthenticationResult.unauthenticated();
    }

    public URL getCallbackUrl() throws MalformedURLException {
        String result = WebUtil.combineContextPath(rootUrlHolder.getRootUrl(), SamlPluginConstants.SAML_CALLBACK_URL.replace("**", ""));
        if (result.startsWith("/")) {
            result = result.substring(1);
        }

        return new URL(result);
    }

    public Saml2Settings buildSettings() throws IOException {
        var pluginSettings = settingsStorage.load();

        return buildSettings(pluginSettings, getCallbackUrl());
    }

    @NotNull
    public static Saml2Settings buildSettings(SamlPluginSettings pluginSettings, URL callbackUrl) throws IOException {
        Map<String, Object> samlData = new HashMap<>();
        samlData.put(SettingsBuilder.IDP_SINGLE_SIGN_ON_SERVICE_URL_PROPERTY_KEY, pluginSettings.getSsoEndpoint());
        samlData.put(SettingsBuilder.IDP_ENTITYID_PROPERTY_KEY, pluginSettings.getIssuerUrl());
        samlData.put(SettingsBuilder.SP_ENTITYID_PROPERTY_KEY, pluginSettings.getEntityId());
        samlData.put(SettingsBuilder.SP_ASSERTION_CONSUMER_SERVICE_URL_PROPERTY_KEY, callbackUrl);
        samlData.put(SettingsBuilder.IDP_X509CERT_PROPERTY_KEY, pluginSettings.getPublicCertificate());
        samlData.put(SettingsBuilder.COMPRESS_REQUEST, pluginSettings.isCompressRequest());
        samlData.put(SettingsBuilder.STRICT_PROPERTY_KEY, pluginSettings.isStrict());

        for (int i = 0; i < pluginSettings.getAdditionalCerts().size(); i++) {
            var cert = pluginSettings.getAdditionalCerts().get(i);
            samlData.put(SettingsBuilder.IDP_X509CERTMULTI_PROPERTY_KEY + "." + i, cert);
        }

        var builder = new SettingsBuilder();
        var samlSettings = builder.fromValues(samlData).build();

        var errors = samlSettings.checkSettings();

        if (!errors.isEmpty()) {
            throw new IOException(String.format("Failed to configure SAML processor: %s", String.join(", ", errors)));
        }

        return samlSettings;
    }

    public void importMetadataIntoSettings(String metadataXml, SamlPluginSettings settings) throws XPathException, CertificateEncodingException {
        var documentMetadata = Util.loadXML(metadataXml);

        Map<String, Object> metadataInfo = IdPMetadataParser.parseXML(documentMetadata);

        var saml2Settings = new Saml2Settings();
        IdPMetadataParser.injectIntoSettings(saml2Settings, metadataInfo);

        settings.setIssuerUrl(saml2Settings.getIdpEntityId());
        settings.setSsoEndpoint(saml2Settings.getIdpSingleSignOnServiceUrl().toString());

        settings.setPublicCertificate(null);
        X509Certificate primaryCertificate = saml2Settings.getIdpx509cert();
        if (primaryCertificate != null) {
            settings.setPublicCertificate(getCertificateBase64Encoded(primaryCertificate));
        }

        if (saml2Settings.getIdpx509certMulti() != null) {
            settings.getAdditionalCerts().clear();
            for(X509Certificate cert : saml2Settings.getIdpx509certMulti()) {
                var encoded = getCertificateBase64Encoded(cert);
                settings.getAdditionalCerts().add(encoded);
            }
        }
    }

    public String getCertificateBase64Encoded(X509Certificate cert) throws CertificateEncodingException {
        var encoded = cert.getEncoded();
        var base64encoded = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encoded);
        if (!StringUtil.isEmpty(base64encoded)) {
            return "-----BEGIN CERTIFICATE-----\n" + base64encoded + "\n-----END CERTIFICATE-----\n";
        }

        return "";
    }

    public boolean isConfigured() {
        return loginConfiguration
                .getConfiguredAuthModules(AuthModuleType.class).stream()
                .anyMatch(t -> t.getType().getClass().getName().equals(SamlAuthenticationScheme.class.getName()));
    }


}
