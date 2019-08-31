import {ISettingsApiService} from "@/services/ISettingsApiService";
import SettingsApiServiceStub from "@/services/SettingsApiService.Stub";
import SettingsApiServiceImpl from "@/services/SettingsApiService.Impl";

export class AppConfig {
    public settingsApiService?: ISettingsApiService;
}

export const appConfig: AppConfig = {};

function devDependencies() {
    appConfig.settingsApiService = new SettingsApiServiceStub();
}

function prodDependencies() {
    appConfig.settingsApiService = new SettingsApiServiceImpl();
}

export function buildDependencies() {
    if (process.env.NODE_ENV === "development") {
        devDependencies();
    } else {
        prodDependencies();
    }
}
