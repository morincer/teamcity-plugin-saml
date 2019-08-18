package jetbrains.buildServer.auth.saml.plugin;

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
    private Boolean hideLoginForm;
}
