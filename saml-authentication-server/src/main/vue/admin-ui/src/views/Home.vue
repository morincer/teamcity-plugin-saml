<template>
    <div>
      <NewSAMLConnectionWizard/>
    </div>
</template>

<style>
</style>

<script lang="ts">
    import SAMLSettings from "@/components/SAMLSettings.vue";
    import NewSAMLConnectionWizard from "@/components/NewSAMLConnectionWizard.vue";
    export default {
        name: 'PageHome',
      components: {NewSAMLConnectionWizard, SAMLSettings}
    }
</script>
