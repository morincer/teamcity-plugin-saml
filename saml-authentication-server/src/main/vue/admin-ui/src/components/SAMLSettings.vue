<template>
    <q-page class="flex flex-center">
        <q-form class="q-gutter-md">
            <q-input v-model="name" label="Name *"/>

            <div>
                <q-btn label="Submit" type="submit" color="primary"/>
                <q-btn label="Reset" type="reset" color="primary" flat class="q-ml-sm" />
            </div>
        </q-form>
    </q-page>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Component from "vue-class-component";

    @Component({})
    export default class SAMLSettings extends Vue {
        name: string = "";
        surname: string = "";
    }
</script>

<style scoped>

</style>
