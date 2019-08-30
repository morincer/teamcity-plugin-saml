<template>
    <tr>
        <th>
            <slot name="label"/>
        </th>
        <td>
            <div>
                <slot name="content"></slot>
            </div>
            <span class="smallNote" v-if="this.$slots['note']">
                <slot name="note"/>
            </span>
        </td>
    </tr>
</template>

<script lang="ts">
    import Vue from "vue";
    import Component from "vue-class-component";

    @Component({})
export default class RunnerFormRow extends Vue {
}
</script>

<style scoped>

</style>
