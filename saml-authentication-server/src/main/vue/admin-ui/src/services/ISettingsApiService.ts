export interface ApiError {
    code: number;
    message: string;
}

export interface ApiCallResult<T> {
    errors?: ApiError[];
    result?: T;
}

export interface SamlSettings {
    issuerUrl?: string;
    entityId?: string;
    ssoEndpoint?: string;
    publicCertificate?: string;
    ssoCallbackUrl? : string;
    hideLoginForm?: boolean;
    ssoLoginButtonName? : string;

    usernameAttribute? : string;
    createUsersAutomatically? : boolean;
    limitToEmailDomains? : boolean;
    allowedEmailDomains? : string;
}

export interface ISettingsApiService {
    get(): Promise<ApiCallResult<SamlSettings>>;
    save(settings: SamlSettings): Promise<ApiCallResult<SamlSettings>>;
}

