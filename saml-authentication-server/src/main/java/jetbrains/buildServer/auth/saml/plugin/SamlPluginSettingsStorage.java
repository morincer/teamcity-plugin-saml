package jetbrains.buildServer.auth.saml.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.serverSide.ServerPaths;
import lombok.var;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SamlPluginSettingsStorage {
    private final ObjectMapper objectMapper;
    private final Path configPath;

    public SamlPluginSettingsStorage(ServerPaths serverPaths) {
        this.objectMapper = new ObjectMapper();
        this.configPath = Paths.get(serverPaths.getConfigDir(), SamlPluginConstants.CONFIG_FILE_NAME);
    }

    public void init() throws IOException {
        load();
    }

    public SamlPluginSettings load() throws IOException {
        if (!this.configPath.toFile().exists()) {
            save(new SamlPluginSettings());
        }

        return this.objectMapper.readValue(this.configPath.toFile(), SamlPluginSettings.class);
    }

    public void save(SamlPluginSettings settings) throws IOException {
        this.objectMapper.writeValue(this.configPath.toFile(), settings);
    }
}
