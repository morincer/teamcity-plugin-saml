package org.gromozeka.teamcity.saml.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.serverSide.ServerPaths;
import lombok.Getter;
import lombok.Setter;
import org.gromozeka.teamcity.saml.plugin.SamlPluginConstants;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.intellij.openapi.application.PathManager.getConfigPath;

public class SamlPluginSettingsStorage {
    private final ObjectMapper objectMapper;
    private final Path configPath;
    private ServerPaths serverPaths;

    private SamlPluginSettings settings;

    public SamlPluginSettingsStorage(ServerPaths serverPaths) {
        this.serverPaths = serverPaths;
        this.objectMapper = new ObjectMapper();
        this.configPath = Paths.get(this.serverPaths.getConfigDir(), SamlPluginConstants.CONFIG_FILE_NAME);
        this.settings = null;
    }

    public void init() throws IOException {
        load();
    }

    public SamlPluginSettings load() throws IOException {
        if (this.settings == null) {
            if (!this.configPath.toFile().exists()) {
                save(new SamlPluginSettings());
            }

            this.settings = this.objectMapper.readValue(this.configPath.toFile(), SamlPluginSettings.class);
        }

        return this.settings;
    }

    public void save(SamlPluginSettings settings) throws IOException {
        this.objectMapper.writeValue(this.configPath.toFile(), settings);
        this.settings = settings;
    }

}
