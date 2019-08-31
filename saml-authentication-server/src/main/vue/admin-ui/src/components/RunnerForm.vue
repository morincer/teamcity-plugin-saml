<template>
    <div>
        <table class="runnerFormTable">
            <slot></slot>
        </table>
        <div class="saveButtonsBlock" v-if="this.$slots['actions']">
            <slot name="actions"></slot>
        </div>
    </div>
</template>

<script lang="ts">
export default {
    name: "RunnerForm",
};
</script>
