<template>
    <q-form class="q-gutter-md" @submit="onSubmit">

        <q-banner v-if="errorMessage" dense class="text-white bg-red">{{errorMessage}}</q-banner>

        <div class="bg-grey-2 q-pa-sm">
            <div class="text-subtitle1 text-grey-10">Identify Provider Configuration</div>
        </div>

        <div>
            <q-input label="Single Sign-on URL" dense/>
            <q-input label="Issuer URL (Identity Provider Entity Id)" dense/>
            <q-input label="X509 Certificate" type="textarea" dense/>
        </div>

        <div class="bg-grey-2 q-pa-sm">
            <div class="text-subtitle1 text-grey-10">Service Provider Configuration</div>
        </div>

        <div>
            <q-input label="Entity ID (Audience)" dense/>
            <q-input label="Single Sign-On URL (Recepient)" dense/>
        </div>

        <div v-bind:disabled="inProgress">
            <q-btn type="submit" label="Save" color="primary" v-bind:loading="inProgress"/>
            <q-btn type="reset" label="Cancel" flat color="primary" class="q-ml-sm"/>
        </div>
    </q-form>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Component from "vue-class-component";

    @Component({})
    export default class SAMLSettings extends Vue {
        errorMessage: string = "";
        inProgress: boolean = false;

        onSubmit() {
            this.inProgress = true;
            setTimeout(() => {
                this.inProgress = false;
                this.errorMessage = "Something wrong happened";
            }, 1000)
        }
    }
</script>

<style scoped>

</style>
