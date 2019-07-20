import axios from 'axios';

export interface ApiError {
    code: number
    message: string
}

export interface ApiCallResult<T> {
    errors?: ApiError[]
    result?: T
}

export interface SamlSettings {
    issuerUrl?: string;
    entityId?: string;
    ssoEndpoint?: string;
    publicCertificate?: string;
}

export interface SettingsApiService {
    get() : Promise<ApiCallResult<SamlSettings>>
    save(settings: SamlSettings) : Promise<ApiCallResult<string>>
}

class MockSettingsApiService implements SettingsApiService{
    async get(): Promise<ApiCallResult<SamlSettings>> {
        let response = await axios
            .get('/mocks/get.json');
        return response.data;
    }

    async save(settings: SamlSettings): Promise<ApiCallResult<string>> {
        let response = await axios.get('/mocks/save.json');
        return response.data;
    }

}

export class SettingsApiServiceImpl implements SettingsApiService {
    constructor(private url: string) {}

    get(): Promise<ApiCallResult<SamlSettings>> {
        return axios.get(`${this.url}?action=get`).then(res => res.data);
    }

    save(settings: SamlSettings): Promise<ApiCallResult<string>> {
        return axios.post(`${this.url}?action=save`, settings).then(res => res.data);
    }


}

export const mockSettingsApiService = new MockSettingsApiService();
