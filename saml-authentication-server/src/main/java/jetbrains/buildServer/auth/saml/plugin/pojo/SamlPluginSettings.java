package jetbrains.buildServer.auth.saml.plugin.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SamlPluginSettings {

    @NotEmpty(message = "Issuer URL is mandatory")
    private String issuerUrl;

    @NotEmpty(message = "Entity Id is mandatory")
    private String entityId;

    @NotEmpty(message = "SSO endpoint is mandatory")
    private String ssoEndpoint;

    @NotEmpty(message = "X509 certificate is mandatory")
    private String publicCertificate;

    private List<String> additionalCerts = new ArrayList<>();

    private String ssoCallbackUrl; // Used for UI-purposes only - calculated automatically

    private boolean hideLoginForm;

    @NotEmpty(message = "Login button name is mandatory")
    private String ssoLoginButtonName = "Login with SSO";

    private boolean createUsersAutomatically = false;
    private boolean assignGroups = false;
    private boolean removeUnassignedGroups = true;
    private boolean limitToPostfixes = false;
    private String allowedPostfixes = null;
    private boolean compressRequest = true;
    private boolean strict = true;
    private boolean samlCorsFilter = true;

    SamlAttributeMappingSettings emailAttributeMapping = new SamlAttributeMappingSettings();
    SamlAttributeMappingSettings nameAttributeMapping = new SamlAttributeMappingSettings();
    SamlAttributeMappingSettings groupsAttributeMapping = new SamlAttributeMappingSettings();
    SamlAttributeMappingSettings vcsUsernameAttributeMapping = new SamlAttributeMappingSettings();

}
