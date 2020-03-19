package jetbrains.buildServer.auth.saml.plugin.pojo;

import lombok.Data;
import javax.validation.constraints.NotEmpty;

@Data
public class SamlPluginSettings {

    @NotEmpty(message = "Issuer URL is mandatory")
    private String issuerUrl;

    @NotEmpty(message = "Entity Id is mandatory")
    private String entityId;

    @NotEmpty(message = "SSO endpoint is mandatory")
    private String ssoEndpoint;

    @NotEmpty(message = "X509 certificate is mandatory")
    private String publicCertificate;

    private String ssoCallbackUrl; // Used for UI-purposes only - calculated automatically

    private boolean hideLoginForm;

    @NotEmpty(message = "Login button name is mandatory")
    private String ssoLoginButtonName = "Login with SSO";

    // Whether we use a dedicated SSO URL for the application or send AuthnRequest
    private boolean dedicatedSsoUrlMode = true;

    private boolean createUsersAutomatically = false;
    private boolean limitToPostfixes = false;
    private String allowedPostfixes = null;

    SamlAttributeMappingSettings emailAttributeMapping = new SamlAttributeMappingSettings();
    SamlAttributeMappingSettings nameAttributeMapping = new SamlAttributeMappingSettings();

}
