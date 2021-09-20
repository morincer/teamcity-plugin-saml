import {ApiCallResult, ISettingsApiService, SamlSettings, SamlSettingsResponse} from "@/services/ISettingsApiService";

export default class SettingsApiServiceStub implements ISettingsApiService {
    public get(): Promise<ApiCallResult<SamlSettingsResponse>> {
        return new Promise<ApiCallResult<SamlSettingsResponse>>(((resolve) => {
            setTimeout(() => {
                resolve({
                    result: {
                        settings: {
                            ssoEndpoint: "some endpoint",
                            entityId: "some entity id",
                            publicCertificate: "some public certificate",
                            issuerUrl: "some issuer url",
                            ssoCallbackUrl: "some callback url",
                            hideLoginForm: false,
                            ssoLoginButtonName: "Login with SSO",
                            additionalCerts: ["cert1", "cert2"],
                            strict: true,
                        },
                        csrfToken: "token",
                        readonly: false,
                    },
                });
            }, 1000);
        }));
    }

    public save(settings: SamlSettings): Promise<ApiCallResult<SamlSettings>> {
        return new Promise<ApiCallResult<SamlSettings>>((resolve) => {
            setTimeout(() => {
                if (settings.ssoEndpoint === "error") {
                    resolve({
                        errors: [{message: "Some error", code: 100}, {message: "ANother error", code: 200}],
                    });
                } else {
                    resolve({result: settings});
                }
            }, 500);
        });
    }

    public importMetadata(metadata: string): Promise<ApiCallResult<SamlSettings>> {
        if (!metadata) {
            return new Promise<ApiCallResult<SamlSettings>>((resolve) => {
                setTimeout(() => {
                    resolve({
                        errors: [
                            {code: 0, message: "Field is required"},
                        ],
                    });
                }, 1000);
            });
        }
        return new Promise<ApiCallResult<SamlSettings>>(((resolve) => {
            setTimeout(() => {
                resolve({
                    result: {
                        ssoEndpoint: "some endpoint from metadata",
                        entityId: "some entity id from metadata",
                        publicCertificate: "some public certificate from metadata",
                        issuerUrl: "some issuer url from metadata",
                        ssoCallbackUrl: "some callback url from metadata",
                        hideLoginForm: false,
                        additionalCerts: ["cert1", "cert2"],
                        ssoLoginButtonName: "Login with SSO",
                        strict: true,
                    },
                });
            }, 1000);
        }));
    }
}
