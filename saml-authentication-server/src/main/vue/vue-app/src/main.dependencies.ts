import 'reflect-metadata';
import {container} from 'inversify-props';
import {ISettingsApiService} from "@/services/ISettingsApiService";
import SettingsApiServiceStub from "@/services/SettingsApiService.Stub";
import {SettingsApiServiceImpl} from "@/services/SettingsApiService.Impl";
import {interfaces} from "inversify";
import {cacheId, injectId} from "inversify-props/src/lib/helpers";

export const Dependencies = {
    SettingsApiService: "SettingsApiService"
};

function devDependencies() {
    console.log('Registering stub services for development');
    container.addSingleton<ISettingsApiService>(SettingsApiServiceStub, Dependencies.SettingsApiService);
}

function prodDependencies() {
    console.log('Registering production services');
    container.addSingleton<ISettingsApiService>(SettingsApiServiceImpl, Dependencies.SettingsApiService);
}

export function buildDependencies() {
    if (process.env.NODE_ENV == 'development') {
        devDependencies();
    } else {
        prodDependencies();
    }
}
