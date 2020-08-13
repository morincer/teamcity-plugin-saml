import axios, {AxiosInstance, AxiosStatic} from "axios";
import {ApiCallResult, ISettingsApiService, SamlSettings, SamlSettingsResponse} from "@/services/ISettingsApiService";

export default class SettingsApiServiceImpl implements ISettingsApiService {
    public url: string = window.location.href.replace(/\/admin.*/, process.env.VUE_APP_SERVICE_URL);

    private readonly instance: AxiosInstance;

    public constructor() {
        this.instance = axios.create();
        this.instance.defaults.headers.common["X-Requested-With"] = "XMLHttpRequest";
    }

    public get(): Promise<ApiCallResult<SamlSettingsResponse>> {
        return this.instance.get(`${this.url}?action=get`).then((res) => {
            const response = res.data as ApiCallResult<SamlSettingsResponse>;
            if (response.result != null && response.result.csrfToken !== "") {
                this.instance.defaults.headers.common["X-TC-CSRF-Token"] = response.result.csrfToken;
            }
            return res.data;
        });
    }

    public save(settings: SamlSettings): Promise<ApiCallResult<SamlSettings>> {
        return this.instance.post(`${this.url}?action=save`, settings).then((res) => res.data);
    }

    public importMetadata(metadata: string): Promise<ApiCallResult<SamlSettings>> {
        return this.instance.post(`${this.url}?action=import`, { metadataXml: metadata }).then((res) => res.data);
    }


}
