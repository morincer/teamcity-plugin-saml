<template>
    <div>

        <MessagesBox :errors="errors" :successMessage="successMsg"/>

        <RunnerForm>
            <RunnerFormRow>
                <template v-slot:label>Metadata XML</template>
                <template v-slot:content>
                    <TextInput textarea rows="10"/>
                    <input type="submit" class="btn btn_primary submitButton"
                           value="Import"
                           :disabled="isSaving"
                           style="margin-left: 10px; vertical-align: top"
                           @click="importMetadata()"
                    />
                    <ProgressIndicator v-if="isSaving"/>
                </template>
                <template v-slot:note>
                    Paste metadata XML into the field
                </template>
            </RunnerFormRow>
        </RunnerForm>
        <RunnerForm v-if="successMsg">
            <GroupingHeader>Identity Provider Configuration</GroupingHeader>
            <RunnerFormInput label="Single Sign-on URL" v-model="settings.ssoEndpoint" required/>
            <RunnerFormInput label="Issuer URL (Identity Provider Entity Id)" required v-model="settings.issuerUrl"/>
            <RunnerFormInput label="X509 Certificate" textarea required v-model="settings.publicCertificate" />

            <GroupingHeader>Service Provider Configuration</GroupingHeader>
            <RunnerFormInput label="Entity ID (Audience)" required v-model="settings.entityId"/>
            <RunnerFormRow>
                <template v-slot:label>Single Sign-On URL (Recipient)</template>
                <template v-slot:content>{{settings.ssoCallbackUrl}}</template>
            </RunnerFormRow>
        </RunnerForm>

    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import RunnerForm from "@/components/RunnerForm.vue";
    import GroupingHeader from "@/components/GroupingHeader.vue";
    import RunnerFormRow from "@/components/RunnerFormRow.vue";
    import RunnerFormInput from "@/components/RunnerFormInput.vue";
    import {Component, Prop} from "vue-property-decorator";
    import {ApiError, ISettingsApiService, SamlSettings} from "@/services/ISettingsApiService";
    import TextInput from "@/components/TextInput.vue";
    import ProgressIndicator from "@/components/ProgressIndicator.vue";
    import MessagesBox from "@/components/MessagesBox.vue";
    import {appConfig} from "@/main.dependencies";
    import SamlAttributeSelect from "@/components/SamlAttributeSelect.vue";

    @Component({ components: {
        SamlAttributeSelect,
        MessagesBox,
        TextInput, RunnerFormInput, RunnerFormRow, GroupingHeader, RunnerForm, ProgressIndicator}})
export default class ImportMetadata extends Vue {

    public settingsApiService: ISettingsApiService = appConfig.settingsApiService!;

    @Prop()
    public settings?: SamlSettings;

    public isLoading: boolean = false;
    public isSaving: boolean = false;
    public successMsg: string = "";
    public errors: ApiError[] = [];

    public metadata: string = "";

    public async importMetadata() {
        try {
            this.isSaving = true;
            const result = await this.settingsApiService.importMetadata(this.metadata);

            if (result.result) {
                this.settings = result.result;
                this.successMsg = "Metadata Imported Successfully. " +
                    "Please review the resulting settings and save them if everything is ok";
            }
        } catch (e) {
            this.errors = [ { message: e, code: 0 }];
        } finally {
            this.isSaving = false;
        }
    }
}
</script>

<style scoped>

</style>
