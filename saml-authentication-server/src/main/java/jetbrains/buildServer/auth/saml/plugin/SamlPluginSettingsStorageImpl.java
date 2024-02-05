package jetbrains.buildServer.auth.saml.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.IOGuard;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.IOGuard;
import lombok.Getter;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;

public class SamlPluginSettingsStorageImpl implements SamlPluginSettingsStorage {
    private final ObjectMapper objectMapper;

    @Nullable
    private volatile SamlPluginSettings cachedSamlPluginSettings = null;

    private final Object lock = new Object();

    @Getter
    private final Path configPath;
    private final FileWatcher samlPluginSettingsFileWatcher;

    public SamlPluginSettingsStorageImpl(Path configPath) {
        this.objectMapper = new ObjectMapper();
        this.configPath = configPath;

        samlPluginSettingsFileWatcher = new FileWatcher(this.configPath.toFile());
    }

    @Override
    public void init() throws IOException {
        load();

        samlPluginSettingsFileWatcher.registerListener(s -> {
            try {
                reload();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        samlPluginSettingsFileWatcher.start();
    }

    @Override
    public SamlPluginSettings load() throws IOException {
        if (cachedSamlPluginSettings != null) return cachedSamlPluginSettings;

        synchronized (lock) {
            if (cachedSamlPluginSettings != null) return cachedSamlPluginSettings;

            return reload();
        }
    }

    @NotNull
    private SamlPluginSettings reload() throws IOException {
        if (!this.configPath.toFile().exists() || this.configPath.toFile().length() == 0) {
            save(new SamlPluginSettings());
        }

        try {
            SamlPluginSettings result = this.objectMapper.readValue(this.configPath.toFile(), SamlPluginSettings.class);

            // some clean-up of additional certs
            if (result.getAdditionalCerts().stream().allMatch(StringUtils::isEmpty)) {
                result.getAdditionalCerts().clear();
            }

            cachedSamlPluginSettings = result;
            return result;
        } catch (RuntimeException ex) {
            Loggers.SERVER.error("Cannot load SAML plugin settings", ex);
            throw ex;
        }
    }

    @Override
    public void save(SamlPluginSettings settings) {
        synchronized (lock) {
            samlPluginSettingsFileWatcher.runActionWithDisabledObserver(() -> {
                try {
                    IOGuard.allowDiskWrite(() -> this.objectMapper.writeValue(this.configPath.toFile(), settings));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            cachedSamlPluginSettings = settings;
        }
    }
}
