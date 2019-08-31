<template>
    <div>

        <MessagesBox :errors="errors" :successMessage="successMsg"/>

        <RunnerForm>
            <RunnerFormRow>
                <template v-slot:label>Metadata XML</template>
                <template v-slot:content>
                    <TextInput textarea rows=10 v-model="metadata"/>
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

            <template v-if="successMsg">
                <GroupingHeader>Identity Provider Configuration Preview</GroupingHeader>
                <RunnerFormInput label="Single Sign-on URL" v-model="previewSettings.ssoEndpoint" readonly/>
                <RunnerFormInput label="Issuer URL (Identity Provider Entity Id)" required
                                 v-model="previewSettings.issuerUrl"/>
                <RunnerFormInput label="X509 Certificate" textarea required
                                 v-model="previewSettings.publicCertificate"/>
            </template>

            <template v-slot:actions>
                <input type="submit" value="Save" class="btn btn_primary submitButton"
                       :disabled="isLoading || isSaving || !previewSettings.ssoEndpoint"
                       @click="save()"/>

                <input type="submit" value="Cancel" class="btn submitButton"
                       @click="cancel()"/>
            </template>
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

    @Component({
        components: {
            SamlAttributeSelect,
            MessagesBox,
            TextInput, RunnerFormInput, RunnerFormRow, GroupingHeader, RunnerForm, ProgressIndicator
        }
    })
    export default class ImportMetadata extends Vue {

        public settingsApiService: ISettingsApiService = appConfig.settingsApiService!;

        public isLoading: boolean = false;
        public isSaving: boolean = false;
        public successMsg: string = "";
        public errors: ApiError[] = [];

        public metadata: string = "";

        previewSettings: SamlSettings = {};

        public async importMetadata() {
            try {
                this.isLoading = true;
                const result = await this.settingsApiService.importMetadata(this.metadata);

                if (result.result) {
                    this.previewSettings = result.result;
                    this.successMsg = "Metadata Imported Successfully. " +
                        "Please review the resulting settings and save them if everything is ok";
                } else if (result.errors) {
                    this.errors = result.errors;
                }
            } catch (e) {
                this.errors = [{message: e, code: 0}];
            } finally {
                this.isLoading = false;
            }
        }

        public async save() {
            try {
                this.isSaving = true;
                const result = await this.settingsApiService.save(this.previewSettings);

                if (result.result) {
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
