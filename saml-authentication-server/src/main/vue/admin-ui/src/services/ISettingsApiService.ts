export interface ApiError {
    code: number;
    message: string;
}

export interface ApiCallResult<T> {
    errors?: ApiError[];
    result?: T;
}

export interface SamlAttributeMapping {
    mappingType: string;
    customAttributeName?: string;
}

export const SamlAttributeMappingTypes = {
    None: "none",
    NameID: "name_id",
    Expression: "expression",
    Other: "other",
};

export interface SamlSettingsResponse {
    settings: SamlSettings;
    csrfToken: string;
    readonly: boolean;
}

export interface SamlSettings {
    issuerUrl?: string;
    entityId?: string;
    ssoEndpoint?: string;
    publicCertificate?: string;
    additionalCerts?: string[];
    ssoCallbackUrl?: string;
    hideLoginForm?: boolean;
    ssoLoginButtonName?: string;
    strict?: boolean;

    samlCorsFilter?: boolean;
    compressRequest?: boolean;

    createUsersAutomatically?: boolean;
    limitToPostfixes?: boolean;
    allowedPostfixes?: string;

    emailAttributeMapping?: SamlAttributeMapping;
    nameAttributeMapping?: SamlAttributeMapping;
    vcsUsernameAttributeMapping?: SamlAttributeMapping;
}

export interface ISettingsApiService {
    get(): Promise<ApiCallResult<SamlSettingsResponse>>;
    save(settings: SamlSettings): Promise<ApiCallResult<SamlSettings>>;
    importMetadata(metadata: string): Promise<ApiCallResult<SamlSettings>>;
}

