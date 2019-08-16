<template>
    <div>

        <MessagesBox :errors="errors" :successMessage="successMsg"/>

        <RunnerForm>
            <GroupingHeader>Identity Provider Configuration</GroupingHeader>
            <RunnerFormInput label="Single Sign-on URL *" v-model="settings.ssoEndpoint"/>
            <RunnerFormInput label="Issuer URL (Identity Provider Entity Id) *" :value="settings.issuerUrl"/>
            <RunnerFormInput label="X509 Certificate *" textarea :value="settings.publicCertificate" />

            <GroupingHeader>Service Provider Configuration</GroupingHeader>
            <RunnerFormInput label="Entity ID (Audience)" :value="settings.entityId"/>
            <RunnerFormRow>
                <template v-slot:label>Single Sign-On URL (Recepient)</template>
            </RunnerFormRow>

            <template v-slot:actions>
                <input type="submit" value="Save" class="btn btn_primary submitButton"
                       :disabled="isLoading || isSaving"
                        @click="submit()"/>
                <ProgressIndicator title="Fetching SSO configuration..." v-if="isLoading"/>
                <ProgressIndicator title="Saving..." v-if="isSaving"/>
            </template>
        </RunnerForm>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import RunnerForm from '@/components/RunnerForm.vue';
import GroupingHeader from '@/components/GroupingHeader.vue';
import RunnerFormRow from '@/components/RunnerFormRow.vue';
import RunnerFormInput from '@/components/RunnerFormInput.vue';
import {Component} from 'vue-property-decorator';
import {ApiError, ISettingsApiService, SamlSettings} from '@/services/ISettingsApiService';
import {cid, container, Inject} from "inversify-props";
import TextInput from "@/components/TextInput.vue";
import {Dependencies} from "@/main.dependencies";
import ProgressIndicator from "@/components/ProgressIndicator.vue";
import MessagesBox from "@/components/MessagesBox.vue";

@Component({ components: {
        MessagesBox,
        TextInput, RunnerFormInput, RunnerFormRow, GroupingHeader, RunnerForm, ProgressIndicator}})
export default class SamlPluginSettings extends Vue {

    @Inject(Dependencies.SettingsApiService)
    settingsApiService!: ISettingsApiService;
    settings: SamlSettings = {};
    isLoading: boolean = false;
    isSaving: boolean = false;
    errors: ApiError[] = [];
    successMsg: string = '';

    async mounted() {
        try {
            this.isLoading = true;
            const result = await this.settingsApiService.get();

            if (result.result) {
                console.log("Recieved response", result);
                this.settings = result.result;
            }
        } catch (e) {

        } finally {
            this.isLoading = false;
        }
    }

    async submit() {
        this.isSaving = true;
        this.errors = [];
        this.successMsg = "";

        const result = await this.settingsApiService.save(this.settings);
        this.isSaving = false;
        console.log(result);
        if (result.result) {
            this.successMsg = "Configuration is saved";
        } else if (result.errors){
            this.errors = result.errors;
        }
    }


}
</script>

<style scoped>

</style>
