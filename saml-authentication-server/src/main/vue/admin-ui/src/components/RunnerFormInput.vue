<template>
    <RunnerFormRow>
        <template v-slot:label>
            {{label}}<span class="mandatoryAsterix" v-if="required">&nbsp;*</span>
        </template>
        <template v-slot:content>
            <TextInput v-bind:value="value"
                       v-on:input="$emit('input', $event)"
                       :short="short" :textarea="textarea"/>
        </template>
        <template v-slot:note>
            <span v-if="note">{{note}}</span>
            <span class="error" v-if="errorMessage">{{errorMessage}}</span>
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

    @Prop([Boolean])
    public required?: Boolean;

    @Prop([String])
    public note?: string;
}
</script>

<style scoped>

</style>
