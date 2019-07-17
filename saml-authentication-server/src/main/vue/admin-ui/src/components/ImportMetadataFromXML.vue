<template>
    <q-form class="q-gutter-md" @submit="onSubmit">
        <q-banner v-if="errorMessage" dense class="text-white bg-red">
            {{errorMessage}}
        </q-banner>
        <q-input type="textarea" label="XML Metadata" outlined v-model="xmlMetadata"/>

        <div>
            <q-btn
                    label="Import"
                    v-bind:disable="xmlMetadata === '' || isWorking"
                    v-bind:loading="isWorking"
                    type="submit" color="primary" />
        </div>
    </q-form>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Component from "vue-class-component";

    @Component({})
    export default class ImportMetadataFromXML extends Vue {
        xmlMetadata: string = "";
        errorMessage: string = "";
        isWorking: boolean = false;

        onSubmit() {
            this.isWorking = true;
            setTimeout(() => {
                this.errorMessage = "Import failed due to flash on Sun";
                this.isWorking = false;
            }, 1000)
        }
    }
</script>


<style scoped>

</style>
