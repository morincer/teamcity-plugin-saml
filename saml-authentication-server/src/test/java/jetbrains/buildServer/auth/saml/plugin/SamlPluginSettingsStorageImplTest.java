package jetbrains.buildServer.auth.saml.plugin;

import lombok.var;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class SamlPluginSettingsStorageImplTest {

    private SamlPluginSettingsStorageImpl storage;

    @Before
    public void setUp() throws Exception {
        var tempFile = File.createTempFile("saml-plugin-settings", "test.json");
        tempFile.deleteOnExit();

        this.storage = new SamlPluginSettingsStorageImpl(tempFile.toPath());
        this.storage.init();
    }

    @Test
    public void shouldStoreSettingsInJsonFile() throws IOException {
        var settings = new SamlPluginSettings();
        settings.setEntityId("Test");

        this.storage.save(settings);
        assertTrue(this.storage.getConfigPath().toFile().exists());
    }

    @Test
    public void shouldLoadSettingsFromJsonFile() throws IOException {
        var settings = new SamlPluginSettings();
        var entityId = UUID.randomUUID().toString();
        settings.setEntityId(entityId);

        this.storage.save(settings);

        var actual = this.storage.load();
        assertThat(actual.getEntityId(), equalTo(entityId));
    }
}
