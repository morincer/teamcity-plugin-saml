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
}

export interface ISettingsApiService {
    get(): Promise<ApiCallResult<SamlSettings>>;
    save(settings: SamlSettings): Promise<ApiCallResult<string>>;
}

