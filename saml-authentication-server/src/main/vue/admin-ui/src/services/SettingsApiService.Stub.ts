import {ApiCallResult, SamlSettings, ISettingsApiService} from '@/services/ISettingsApiService';
import set = Reflect.set;

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
                        ssoCallbackUrl: 'some callback url',
                        hideLoginForm: false,
                        ssoLoginButtonName: "Login with SSO"
                    },
                });
            }, 1000);
        }));
    }

    public save(settings: SamlSettings): Promise<ApiCallResult<SamlSettings>> {

        if (process.env.NODE_ENV === 'development') {
            // tslint:disable-next-line:no-console
            console.log(JSON.stringify(settings, null, 3));
        }

        return new Promise<ApiCallResult<SamlSettings>>((resolve) => {
            setTimeout(() => {
                if (settings.ssoEndpoint === 'error') {
                    resolve({
                        errors: [{message: 'Some error', code: 100}, {message: 'ANother error', code: 200}],
                    });
                } else {
                    resolve({result: settings});
                }
            }, 500);
        });
    }
}
