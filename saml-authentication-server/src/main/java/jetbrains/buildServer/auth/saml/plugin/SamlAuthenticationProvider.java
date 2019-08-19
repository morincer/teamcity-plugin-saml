package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;

public class SamlAuthenticationProvider {
    private SamlPluginSettings settings;

    public SamlAuthenticationProvider(SamlPluginSettings settings) {
        this.settings = settings;
    }

    public HttpAuthenticationResult authenticate() {
        return HttpAuthenticationResult.unauthenticated();
    }
}
