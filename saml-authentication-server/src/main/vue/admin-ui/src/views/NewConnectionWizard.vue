<template>
    <div>
        <h3>Choose SSO Provider:</h3>
        <select v-model="provider" style="margin-bottom: 20px">
            <option :value="SsoProvider.Okta">Okta</option>
            <option :value="SsoProvider.OneLogin">OneLogin</option>
            <option disabled>-----------------</option>
            <option :value="SsoProvider.ImportMetadata">Other - Import Metadata</option>
            <option :value="SsoProvider.None">Other - Exit and Setup Manually</option>
        </select>
        <SettingsFacade v-if="provider == SsoProvider.Okta" :settingsDecoration="oktaDecoration" />
        <RunnerForm>
            <template v-slot:actions>
                <input type="submit" value="Save" class="btn btn_primary submitButton"
                       :disabled="isLoading"
                       @click="submit()"/>

                <input type="submit" value="Cancel" class="btn submitButton"
                       :disabled="isLoading"
                       @click="submit()"/>
            </template>
        </RunnerForm>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import {Component} from "vue-property-decorator";
    import SettingsFacade, {SettingsDecoration} from "@/components/SettingsFacade.vue";
    import {SamlSettings} from "@/services/ISettingsApiService";
    import RunnerForm from "@/components/RunnerForm.vue";

    enum SsoProvider {
        None, Okta, OneLogin, ImportMetadata
    }

    const OktaDecoration : SettingsDecoration = {
        introMessage: "Login to Okta administration console. " +
            "Click the Developer Console and choose Classic UI. Go to Applications. " +
            "Choose (or create) the IdP application (i.e. Teamcity). ",
        groups: [
            {
                title: "Okta Identity Provider Settings",
                message: "Go to the Sign On tab and click 'View Setup Instructions' button. Fill the values into fields below.",
                parameters: [
                    { fieldName: "ssoEndpoint", hint: null, label: "Identity Provider Single Sign-On URL:" },
                    { fieldName: "issuerUrl", hint: null, label: "Identity Provider Issuer:" },
                    { fieldName: "publicCertificate", label: "X.509 Certificate:", textarea: true, hint: "Paste full X.509 certificate here" },
                ],
            },
            {
                title: "Teamcity Service Provider Settings",
                message: "Go to the General tab -> SAML Settings and click 'Edit' button -> Next to the Configure SAML step. " +
                    "Fill the values from fields below.",
                parameters: [
                    { fieldName: "ssoCallbackUrl", hint: null, label: "Single sign on URL:", readonly: true },
                    { fieldName: "entityId", hint: null, label: "Audience URI (SP Entity ID):", readonly: true },
                ],
            }
        ]
    };

    @Component({ components: { SettingsFacade, RunnerForm}})
    export default class NewConnectionWizard extends Vue {
        SsoProvider = SsoProvider;
        provider: SsoProvider = this.SsoProvider.Okta;
        settings!: SamlSettings;
        oktaDecoration = OktaDecoration;

    }

</script>

<style scoped>

</style>
