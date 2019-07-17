<template>
    <q-form class="q-gutter-md">
        <q-input label="URL" v-model="url"/>
        <div>
            <q-btn type="submit" color="primary" v-bind:disable="url == ''" label="Download and Import"/>
        </div>
    </q-form>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Component from "vue-class-component";

    @Component({})
    export default class ImportMetadataFromURL extends Vue{
        url: string = "";
    }
</script>

<style scoped>

</style>
