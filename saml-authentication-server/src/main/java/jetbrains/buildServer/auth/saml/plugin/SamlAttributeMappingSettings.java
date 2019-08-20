package jetbrains.buildServer.auth.saml.plugin;

import lombok.Data;

@Data
public class SamlAttributeMappingSettings {
    public static final String TYPE_NONE = "none";
    public static final String TYPE_NAME_ID = "name_id";
    public static final String TYPE_OTHER = "other";

    private String mappingType = TYPE_NONE;
    private String customAttributeName;
}
