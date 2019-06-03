package org.gromozeka.teamcity.saml.core;

import lombok.Getter;
import org.gromozeka.teamcity.saml.core.config.ISamlAuthenticationConfigProvider;

public class SamlAuthenticationProvider {

    @Getter
    private ISamlAuthenticationConfigProvider configProvider;

    public SamlAuthenticationProvider(ISamlAuthenticationConfigProvider configProvider) {
        this.configProvider = configProvider;
    }
}
