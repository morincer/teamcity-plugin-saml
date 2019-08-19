<template>
    <div>

        <MessagesBox :errors="errors" :successMessage="successMsg"/>

        <RunnerForm>
            <GroupingHeader>Identity Provider Configuration</GroupingHeader>
            <RunnerFormInput label="Single Sign-on URL" v-model="settings.ssoEndpoint" required/>
            <RunnerFormInput label="Issuer URL (Identity Provider Entity Id)" required v-model="settings.issuerUrl"/>
            <RunnerFormInput label="X509 Certificate" textarea required v-model="settings.publicCertificate" />

            <GroupingHeader>Service Provider Configuration</GroupingHeader>
            <RunnerFormInput label="Entity ID (Audience)" required v-model="settings.entityId"/>
            <RunnerFormRow>
                <template v-slot:label>Single Sign-On URL (Recepient)</template>
                <template v-slot:content>{{settings.ssoCallbackUrl}}</template>
            </RunnerFormRow>

            <GroupingHeader>Users Mapping and Provisioning</GroupingHeader>
            <RunnerFormRow>
                <template v-slot:label>Create users automatically</template>
                <template v-slot:content><input type="checkbox" v-model="settings.createUsersAutomatically"></template>
                <template v-slot:note>If a user name is missing a new user with this name will be created</template>
            </RunnerFormRow>
            <RunnerFormRow v-if="settings.createUsersAutomatically">
                <template v-slot:label>Limit to specific username postfixes</template>
                <template v-slot:content>
                    <input type="checkbox" v-model="settings.limitToPostfixes">
                    <TextInput style="margin-left: 10px" v-model="settings.allowedPostfixes" v-if="settings.limitToPostfixes"/>
                </template>
                <template v-slot:note>Users will only be created if their names end with particular postfixes - for example, @mymail.com (provide comma-separated list for multiple postfixes) </template>
            </RunnerFormRow>
            <RunnerFormInput v-if="settings.createUsersAutomatically" label="Map Name from" note="Fill new user's name from the specified SAML attribute"/>
            <RunnerFormInput v-if="settings.createUsersAutomatically" label="Map E-Mail from" note="Fill new user's e-mail from the specified SAML attribute"/>

            <GroupingHeader>Misc</GroupingHeader>
            <RunnerFormInput label="Login Button Label" required v-model="settings.ssoLoginButtonName"/>
            <RunnerFormRow>
                <template v-slot:label>Hide Login Form</template>
                <template v-slot:content><input type="checkbox" v-model="settings.hideLoginForm"/></template>
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
import TextInput from '@/components/TextInput.vue';
import ProgressIndicator from '@/components/ProgressIndicator.vue';
import MessagesBox from '@/components/MessagesBox.vue';
import {appConfig} from '@/main.dependencies';

@Component({ components: {
        MessagesBox,
        TextInput, RunnerFormInput, RunnerFormRow, GroupingHeader, RunnerForm, ProgressIndicator}})
export default class SamlPluginSettings extends Vue {

    public settingsApiService: ISettingsApiService = appConfig.settingsApiService!;

    public settings: SamlSettings = {};
    public isLoading: boolean = false;
    public isSaving: boolean = false;
    public errors: ApiError[] = [];
    public successMsg: string = '';

    public async mounted() {
        try {
            this.isLoading = true;
            const result = await this.settingsApiService.get();

            if (result.result) {
                this.settings = result.result;
            }
        } catch (e) {
            this.errors = [ { message: e, code: 0 }];
        } finally {
            this.isLoading = false;
        }
    }

    public async submit() {
        this.isSaving = true;
        this.errors = [];
        this.successMsg = '';

        const result = await this.settingsApiService.save(this.settings);
        this.isSaving = false;
        if (result.result) {
            this.successMsg = 'Your changes have been saved.';
        } else if (result.errors) {
            this.errors = result.errors;
        }
    }


}
</script>

<style scoped>

</style>
