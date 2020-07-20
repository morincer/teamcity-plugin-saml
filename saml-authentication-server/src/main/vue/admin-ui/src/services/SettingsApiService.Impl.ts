import axios, {AxiosInstance, AxiosStatic} from "axios";
import {ApiCallResult, ISettingsApiService, SamlSettings} from "@/services/ISettingsApiService";

export default class SettingsApiServiceImpl implements ISettingsApiService {
    public url: string = process.env.VUE_APP_SERVICE_URL;

    private readonly instance: AxiosInstance;

    public constructor() {
        this.instance = axios.create();
        this.instance.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    }

    public get(): Promise<ApiCallResult<SamlSettings>> {
        return this.instance.get(`${this.url}?action=get`).then((res) => res.data);
    }

    public save(settings: SamlSettings): Promise<ApiCallResult<SamlSettings>> {
        return this.instance.post(`${this.url}?action=save`, settings).then((res) => res.data);
    }

    public importMetadata(metadata: string): Promise<ApiCallResult<SamlSettings>> {
        return this.instance.post(`${this.url}?action=import`, { metadataXml: metadata }).then((res) => res.data);
    }


}
