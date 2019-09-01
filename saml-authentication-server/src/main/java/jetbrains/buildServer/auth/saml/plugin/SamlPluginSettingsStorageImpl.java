package jetbrains.buildServer.auth.saml.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;

public class SamlPluginSettingsStorageImpl implements SamlPluginSettingsStorage {
    private final ObjectMapper objectMapper;

    @Getter
    private final Path configPath;

    public SamlPluginSettingsStorageImpl(Path configPath) {
        this.objectMapper = new ObjectMapper();
        this.configPath = configPath;
    }

    @Override
    public void init() throws IOException {
        load();
    }

    @Override
    public SamlPluginSettings load() throws IOException {
        if (!this.configPath.toFile().exists() || this.configPath.toFile().length() == 0) {
            save(new SamlPluginSettings());
        }

        return this.objectMapper.readValue(this.configPath.toFile(), SamlPluginSettings.class);
    }

    @Override
    public void save(SamlPluginSettings settings) throws IOException {
        this.objectMapper.writeValue(this.configPath.toFile(), settings);
    }
}
