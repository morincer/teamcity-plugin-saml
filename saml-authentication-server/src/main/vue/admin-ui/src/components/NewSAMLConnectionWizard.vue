<template>
    <q-card flat bordered class="my-card">
        <q-card-section>
            <div class="text-h6">New SAML Connection</div>
        </q-card-section>

        <q-card-section>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod
            tempor incididunt ut labore et dolore magna aliqua.
        </q-card-section>

        <q-separator inset/>

        <q-card-section>
            <q-form class="q-gutter-md">
                <q-select label="Identity Provider Type" :options="options" outlined v-model="idpType"/>
            </q-form>
        </q-card-section>

        <q-card-section>
            <ImportMetadataFromXML v-if="idpType.value == wizardPages.ImportMetadataFromXml"/>
            <ImportMetadataFromURL v-if="idpType.value == wizardPages.ImportMetadataFromURL"/>
        </q-card-section>
    </q-card>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Component from "vue-class-component";
    import ImportMetadataFromXML from "@/components/ImportMetadataFromXML.vue";
    import ImportMetadataFromURL from "@/components/ImportMetadataFromURL.vue";

    enum WizardPages {
        ImportMetadataFromXml,
        ImportMetadataFromURL
    }

    interface SelectionOption {
        label: string
        value: WizardPages
    }

    @Component({
        components: {ImportMetadataFromURL, ImportMetadataFromXML}
    })
    export default class NewSAMLConnectionWizard extends Vue {
        wizardPages = WizardPages;
        options : SelectionOption[] = [
            { label: "Import metadata from XML", value: this.wizardPages.ImportMetadataFromXml },
            { label: "Import metadata from URL", value: this.wizardPages.ImportMetadataFromURL}
        ];

        idpType: SelectionOption = this.options[0] ;
    }
</script>

<style scoped>

</style>
