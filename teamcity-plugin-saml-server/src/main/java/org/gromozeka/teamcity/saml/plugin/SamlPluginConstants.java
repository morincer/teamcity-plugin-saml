package org.gromozeka.teamcity.saml.plugin;

public interface SamlPluginConstants {
    String SAML_INITIATE_LOGIN_URL = "/app/saml/login/**";
    String SAML_CALLBACK_URL = "/app/saml/callback/**";
    String SAML_DEFAULT_SP_ENTITY_ID = "teamcity-saml-plugin";
    String PLUGIN_NAME = "SAML Authentication";
}
