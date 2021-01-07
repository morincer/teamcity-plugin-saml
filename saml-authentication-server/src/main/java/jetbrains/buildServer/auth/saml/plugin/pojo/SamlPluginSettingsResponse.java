package jetbrains.buildServer.auth.saml.plugin.pojo;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.Data;

@Data
public class SamlPluginSettingsResponse {
    private SamlPluginSettings settings;
    private String csrfToken = "";
    private boolean isReadonly = false;
}
