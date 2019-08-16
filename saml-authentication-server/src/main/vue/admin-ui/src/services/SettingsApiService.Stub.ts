import {ApiCallResult, SamlSettings, ISettingsApiService} from '@/services/ISettingsApiService';

export default class SettingsApiServiceStub implements ISettingsApiService {
    public get(): Promise<ApiCallResult<SamlSettings>> {
        return new Promise<ApiCallResult<SamlSettings>>(((resolve) => {
            setTimeout(() => {
                resolve({
                    result: {
                        ssoEndpoint: 'some endpoint',
                        entityId: 'some entity id',
                        publicCertificate: 'some public certificate',
                        issuerUrl: 'some issuer url',
                    },
                });
            }, 1000);
        }));
    }

    public save(settings: SamlSettings): Promise<ApiCallResult<string>> {
        return new Promise<ApiCallResult<string>>((resolve) => {
            setTimeout(() => {
                if (settings.ssoEndpoint === 'error') {
                    resolve({
                        errors: [{message: 'Some error', code: 100}, {message: 'ANother error', code: 200}],
                    });
                } else {
                    resolve({result: 'ok'});
                }
            }, 500);
        });
    }
}
