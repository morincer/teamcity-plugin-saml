<template>
    <div>
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
import {ISettingsApiService, SamlSettings} from '@/services/ISettingsApiService';
import {cid, container, Inject} from "inversify-props";
import TextInput from "@/components/TextInput.vue";
import {Dependencies} from "@/main.dependencies";

@Component({ components: {TextInput, RunnerFormInput, RunnerFormRow, GroupingHeader, RunnerForm}})
export default class SamlPluginSettings extends Vue {

    @Inject(Dependencies.SettingsApiService)
    settingsApiService!: ISettingsApiService;
    settings: SamlSettings = {};

    async mounted() {
        const result = await this.settingsApiService.get();
        if (result.result) {
            console.log("Recieved response", result);
            this.settings = result.result;
        }

    }
}
</script>

<style scoped>

</style>
