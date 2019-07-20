<template>
    <q-form @submit="onSubmit">

        <q-banner v-if="errorMessage" dense class="text-white bg-red q-my-lg">{{errorMessage}}</q-banner>

        <q-banner v-if="okMessage" dense class="text-white bg-green-5 q-my-lg">{{okMessage}}</q-banner>

        <div class="bg-grey-2 q-pa-sm">
            <div class="text-subtitle1 text-grey-10">Identify Provider Configuration</div>
        </div>

        <div class="q-gutter-md">
            <q-input label="Single Sign-on URL"
                     v-model="samlSettings.ssoEndpoint"
                    :rules="[val => !!val || 'Field is required']"
            />
            <q-input label="Issuer URL (Identity Provider Entity Id)"
                     v-model="samlSettings.issuerUrl"
                     :rules="[val => !!val || 'Field is required']"/>
            <q-input label="X509 Certificate" type="textarea"
                     v-model="samlSettings.publicCertificate"
                     :rules="[val => !!val || 'Field is required']"/>
        </div>

        <div class="bg-grey-2 q-pa-sm q-mt-md">
            <div class="text-subtitle1 text-grey-10">Service Provider Configuration</div>
        </div>

        <div>
            <q-input label="Entity ID (Audience)" v-model="samlSettings.entityId"/>
            <!--            <q-input label="Single Sign-On URL (Recepient)" dense v-model="samlSettings.ssoEndpoint"/>-->
        </div>

        <div v-bind:disabled="inProgress" class="q-mt-lg">
            <q-btn type="submit" label="Save" color="primary" v-bind:loading="inProgress"/>
            <q-btn type="reset" label="Cancel" flat color="primary" class="q-ml-sm"/>
        </div>
    </q-form>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Component from "vue-class-component";
    import {
        mockSettingsApiService,
        SamlSettings,
        SettingsApiService,
        SettingsApiServiceImpl
    } from "@/services/SettingsApiService";
    import {required} from "vuelidate/lib/validators";

    @Component({})
    export default class SAMLSettings extends Vue {
        errorMessage: string = "";
        okMessage: string = "";
        inProgress: boolean = false;

        service: SettingsApiService = new SettingsApiServiceImpl("/admin/samlSettingsApi.html");

        samlSettings: SamlSettings = {};

        validations = {
            ssoEndpoint: {
                required
            }
        };

        mounted() {
            this.inProgress = true;
            this.service.get().then(result => {
                console.log("Settings received", result);
                this.inProgress = false;
                this.errorMessage = "";
                this.okMessage = "";

                if (result.errors) {
                    console.log(result.errors);
                    this.errorMessage = result.errors.map(e => e.message).join(", ");
                } else {
                    this.samlSettings = result.result as SamlSettings;
                }
            }).catch(err => {
                this.inProgress = false;
                this.errorMessage = err;
            });
        }

        onSubmit() {
            this.inProgress = true;
            this.okMessage = "";
            this.errorMessage = "";
            this.service.save(this.samlSettings).then(res => {
                this.inProgress = false;
                if (res.errors) {
                    this.errorMessage = res.errors.map(e => e.message).join(", ");
                } else {
                    this.okMessage = "Settings Saved..."
                }
            }).catch(err => {
                this.inProgress = false;
                this.errorMessage = err;
            })
        }
    }
</script>

<style scoped>

</style>
