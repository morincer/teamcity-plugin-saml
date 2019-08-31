export interface ParametersGroup {
    message?: string;
    title: string;
    parameters: ParameterDecoration[];
}

export interface ParameterDecoration {
    fieldName: string;
    label: string;
    hint: string;
    textarea?: boolean;
    readonly?: boolean;
}

export interface SettingsDecoration {
    introMessage: string;
    groups: ParametersGroup[];
}


export const OktaDecoration: SettingsDecoration = {
    introMessage: "Login to the Okta administration console. " +
        "Click the Developer Console and choose Classic UI. Go to Applications. " +
        "Choose (or create) the IdP application (i.e. Teamcity). ",
    groups: [
        {
            title: "Okta Identity Provider Settings",
            message: "Go to the Sign On tab and click 'View Setup Instructions' button. " +
                "Copy the values into fields below.",
            parameters: [
                {fieldName: "ssoEndpoint", hint: "", label: "Identity Provider Single Sign-On URL:"},
                {fieldName: "issuerUrl", hint: "", label: "Identity Provider Issuer:"},
                {
                    fieldName: "publicCertificate",
                    label: "X.509 Certificate:",
                    textarea: true,
                    hint: "Paste full X.509 certificate here",
                },
            ],
        },
        {
            title: "Teamcity Service Provider Settings",
            message: "Go to the General tab -> SAML Settings and click 'Edit' button -> " +
                "Next to the Configure SAML step. " +
                "Fill the values from fields below.",
            parameters: [
                {fieldName: "ssoCallbackUrl", hint: "", label: "Single sign on URL:", readonly: true},
                {fieldName: "entityId", hint: "", label: "Audience URI (SP Entity ID):", readonly: true},
            ],
        },
    ],
};

export const OneloginDecoration: SettingsDecoration = {
    introMessage: "Login to the Onelogin administration console. " +
        "Click the Apps. " +
        "Choose (or create) the IdP application (i.e. Teamcity). Click Edit. " +
        "Don't forget to save settings in both Teamcity and Onelogin after you finish configuring",
    groups: [
        {
            title: "Onelogin Identity Provider Settings",
            message: "Go to the SSO tab. Copy the values into fields below.",
            parameters: [
                {fieldName: "ssoEndpoint", hint: "", label: "SAML 2.0 Endpoint (HTTP):"},
                {fieldName: "issuerUrl", hint: "", label: "Issuer URL:"},
                {
                    fieldName: "publicCertificate",
                    label: "X.509 Certificate:",
                    textarea: true,
                    hint: "Click View Details link under the X.509 Certificate field " +
                        "and copy-paste the certificate here ",
                },
            ],
        },
        {
            title: "Teamcity Service Provider Settings",
            message: "Go to the Configuration tab" +
                "Fill the values from fields below.",
            parameters: [
                {fieldName: "entityId", hint: "", label: "Audience:", readonly: true},
                {fieldName: "ssoCallbackUrl", hint: "", label: "ACS (Consumer) URL Validator:", readonly: true},
                {fieldName: "ssoCallbackUrl", hint: "", label: "ACS (Consumer) URL:", readonly: true},
            ],
        },
    ],
};


