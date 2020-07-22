<template>
    <div>
        <MessagesBox :errors="errors" :successMessage="successMsg"/>

        <ProgressIndicator v-if="isLoading" title="Loading current configuration..."/>

        <div v-if="!isLoading">
            <h3>Choose SSO Provider:</h3>
            <select v-model="provider" style="margin-bottom: 20px">
                <option :value="SsoProvider.Okta">Okta</option>
                <option :value="SsoProvider.OneLogin">OneLogin</option>
            </select>
            <SettingsFacade v-if="provider == SsoProvider.Okta" :settingsDecoration="oktaDecoration"
                            :settings="settings"/>
            <SettingsFacade v-else-if="provider == SsoProvider.OneLogin" :settingsDecoration="oneloginDecoration"
                            :settings="settings"/>
            <ImportMetadata v-else-if="provider == SsoProvider.ImportMetadata" :settings="settings" />

            <RunnerForm>
                <template v-slot:actions>
                    <input type="submit" value="Save" class="btn btn_primary submitButton"
                           :disabled="isLoading || isSaving"
                           @click="submit()"/>

                    <input type="submit" value="Cancel" class="btn submitButton"
                           @click="cancel()"/>

                    <ProgressIndicator title="Saving..." v-if="isSaving"/>

                </template>
            </RunnerForm>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from "vue";
import {Component} from "vue-property-decorator";
import SettingsFacade from "@/components/SettingsFacade.vue";
import {ApiError, ISettingsApiService, SamlSettings} from "@/services/ISettingsApiService";
import RunnerForm from "@/components/RunnerForm.vue";
import {appConfig} from "@/main.dependencies";
import MessagesBox from "@/components/MessagesBox.vue";
import ProgressIndicator from "@/components/ProgressIndicator.vue";
import ImportMetadata from "@/views/ImportMetadata.vue";
import {OktaDecoration, OneloginDecoration} from "@/resources/ConnectionWizardDecorations";

enum SsoProvider {
    None, Okta, OneLogin, ImportMetadata,
}


@Component({components: {ImportMetadata, ProgressIndicator, SettingsFacade, RunnerForm, MessagesBox}})
export default class NewConnectionWizard extends Vue {

    public settingsApiService: ISettingsApiService = appConfig.settingsApiService!;

    public SsoProvider = SsoProvider;
    public provider: SsoProvider = this.SsoProvider.Okta;

    public settings: SamlSettings = {};

    public oktaDecoration = OktaDecoration;
    public oneloginDecoration = OneloginDecoration;

    public isLoading: Boolean = false;
    public isSaving: boolean = false;
    public errors: ApiError[] = [];
    public successMsg: string = "";

    public async mounted() {
        try {
            this.isLoading = true;
            const result = await this.settingsApiService.get();

            if (result.result) {
                this.settings = result.result.settings;
            }
        } catch (e) {
            this.errors = [{message: e, code: 0}];
        } finally {
            this.isLoading = false;
        }
    }

    public async submit() {
        try {
            this.isSaving = true;
            const result = await this.settingsApiService.save(this.settings);

            if (result.result) {
                this.settings = result.result;

                this.$router.push("/");
            }
        } catch (e) {
            this.errors = [{message: e, code: 0}];
        } finally {
            this.isSaving = false;
        }
    }

     public cancel() {
         this.$router.push("/");
     }
}
</script>

<style scoped>

</style>
