package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import lombok.var;
import org.springframework.beans.BeanUtils;

import java.io.IOException;

public class InMemorySamlPluginSettingsStorage implements SamlPluginSettingsStorage {

    SamlPluginSettings settings = new SamlPluginSettings();

    @Override
    public void init() throws IOException {
        settings = new SamlPluginSettings();
    }

    @Override
    public SamlPluginSettings load() throws IOException {
        var result = new SamlPluginSettings();
        BeanUtils.copyProperties(this.settings, result);
        return result;
    }

    @Override
    public void save(SamlPluginSettings settings) throws IOException {
        BeanUtils.copyProperties(settings, this.settings);
    }
}
