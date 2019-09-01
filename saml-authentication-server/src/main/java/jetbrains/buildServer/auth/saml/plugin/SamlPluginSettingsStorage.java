package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;

import java.io.IOException;

public interface SamlPluginSettingsStorage {
    void init() throws IOException;

    SamlPluginSettings load() throws IOException;

    void save(SamlPluginSettings settings) throws IOException;
}
