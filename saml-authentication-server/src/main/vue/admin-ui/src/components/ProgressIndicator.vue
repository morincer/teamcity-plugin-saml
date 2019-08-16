<template>
    <span>
        <span v-if="title">{{title}}&nbsp;</span>
        <div class="progressRing progressRingDefault ring-loader-inline"><i class="ring-loader-inline__ball"/></div>
    </span>
</template>

<script lang="ts">
import Vue from 'vue';
import {Prop} from 'vue-property-decorator';
import Component from 'vue-class-component';

@Component({})
export default class ProgressIndicator extends Vue {
    @Prop()
    public title?: string;
}
</script>

<style scoped>

</style>
