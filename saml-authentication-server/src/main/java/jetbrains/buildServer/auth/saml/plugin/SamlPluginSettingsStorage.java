package jetbrains.buildServer.auth.saml.plugin;

import java.io.IOException;

public interface SamlPluginSettingsStorage {
    void init() throws IOException;

    SamlPluginSettings load() throws IOException;

    void save(SamlPluginSettings settings) throws IOException;
}
