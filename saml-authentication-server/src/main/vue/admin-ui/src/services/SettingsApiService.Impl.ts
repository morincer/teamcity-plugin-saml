import axios from 'axios';
import {ApiCallResult, SamlSettings, ISettingsApiService} from '@/services/ISettingsApiService';

export default class SettingsApiServiceImpl implements ISettingsApiService {
    public url: string = process.env.VUE_APP_SERVICE_URL;

    public get(): Promise<ApiCallResult<SamlSettings>> {
        return axios.get(`${this.url}?action=get`).then((res) => res.data);
    }

    public save(settings: SamlSettings): Promise<ApiCallResult<SamlSettings>> {
        return axios.post(`${this.url}?action=save`, settings).then((res) => res.data);
    }

    public importMetadata(metadata: string): Promise<ApiCallResult<SamlSettings>> {
        return axios.post(`${this.url}?action=import`, metadata).then((res) => res.data);
    }


}
