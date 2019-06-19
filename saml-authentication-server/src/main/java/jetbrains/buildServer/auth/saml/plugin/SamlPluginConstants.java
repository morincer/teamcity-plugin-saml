package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.users.AuthPropertyKey;

public interface SamlPluginConstants {
    String SAML_INITIATE_LOGIN_URL = "/app/saml/login/**";
    String SAML_CALLBACK_URL = "/app/saml/callback/**";
    String SAML_DEFAULT_SP_ENTITY_ID = "/teamcity-saml-plugin";
    String PLUGIN_NAME = "SAML Authentication";
    String CONFIG_FILE_NAME = "saml-plugin.config.json";
    String SETTINGS_CONTROLLER_PATH = "/admin/samlSettings.html";
    String AUTH_SCHEME_NAME = "SAML.v2";
    String AUTH_SCHEME_DESCRIPTION = "Provides SAML assertions-based authentication";

    AuthPropertyKey ID_USER_PROPERTY_KEY = new AuthPropertyKey("HTTP", "teamcity-saml-id", "SAML ID");

}
