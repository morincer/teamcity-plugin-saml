<template>
    <RunnerFormRow>
        <template v-slot:label>{{label}}</template>
        <template v-slot:content>
            <TextInput v-bind:value="value"
                       v-on:input="$emit('input', $event)"
                       :short="short" :textarea="textarea"/>
        </template>
        <template v-slot:note v-if="errorMessage">
            <span class="error">{{errorMessage}}</span>
        </template>
    </RunnerFormRow>
</template>

<script lang="ts">
import Vue from 'vue';
import {Prop} from 'vue-property-decorator';
import Component from 'vue-class-component';
import RunnerFormRow from '@/components/RunnerFormRow.vue';
import TextInput from '@/components/TextInput.vue';

@Component({
    components: {TextInput, RunnerFormRow},
})
export default class RunnerFormInput extends Vue {
    @Prop()
    public label?: string;

    @Prop()
    public value?: string;

    @Prop([Boolean])
    public short!: Boolean;

    @Prop([Boolean])
    public textarea!: Boolean;

    @Prop([String])
    public errorMessage?: string;
}
</script>

<style scoped>

</style>
