<template>
    <div>
        <h3>Choose SSO Provider:</h3>
        <select v-model="provider">
            <option :value="SsoProvider.Okta">Okta</option>
            <option :value="SsoProvider.OneLogin">OneLogin</option>
            <option disabled>-----------------</option>
            <option :value="SsoProvider.ImportMetadata">Other - Import Metadata</option>
            <option :value="SsoProvider.None">Other - Exit and Setup Manually</option>
        </select>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import {Component} from "vue-property-decorator";

    enum SsoProvider {
        None, Okta, OneLogin, ImportMetadata
    }

    @Component({})
    export default class NewConnectionWizard extends Vue {
        SsoProvider = SsoProvider;
        provider: SsoProvider = this.SsoProvider.Okta
    }

</script>

<style scoped>

</style>
