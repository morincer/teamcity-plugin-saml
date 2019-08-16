<template>
    <div>
        <q-btn color="secondary" @click="$router.push('/new')">Create new connection</q-btn>
        <SAMLSettings/>
    </div>
</template>

<script lang="ts">
    import Vue from "vue";
    import SAMLSettings from "@/components/SAMLSettings.vue";
    import Component from "vue-class-component";

    @Component({components: { SAMLSettings }})
    export default class Settings extends Vue {

    }

</script>

<style scoped>

</style>
