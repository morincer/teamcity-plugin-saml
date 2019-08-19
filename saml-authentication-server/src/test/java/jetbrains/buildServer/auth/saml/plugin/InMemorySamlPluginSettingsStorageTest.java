package jetbrains.buildServer.auth.saml.plugin;

import lombok.var;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class InMemorySamlPluginSettingsStorageTest {
    private InMemorySamlPluginSettingsStorage settingsStorage;

    @Before
    public void setUp() throws Exception {
        this.settingsStorage = new InMemorySamlPluginSettingsStorage();
        this.settingsStorage.init();
    }

    @Test
    public void shouldSaveAndLoadSettings() throws IOException {
        var settings = new SamlPluginSettings();
        settings.setEntityId(UUID.randomUUID().toString());

        this.settingsStorage.save(settings);

        var actual = this.settingsStorage.load();

        assertNotSame(actual, settings); // objects are different
        assertThat(actual.getEntityId(), equalTo(settings.getEntityId()));
    }
}
