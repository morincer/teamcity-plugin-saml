<template>
    <span>
        <input v-if="!textarea.valueOf()"
               type="text" :class="className" v-bind:value="value"
               v-on:input="$emit('input', $event.target.value)"/>
        <textarea v-else :rows="rows"
                  :class="className" v-bind:value="value"
                  v-on:input="$emit('input', $event.target.value)"/>
    </span>
</template>

<script lang="ts">
    import Vue from "vue";
    import Component from "vue-class-component";
    import {Prop} from "vue-property-decorator";

    @Component({})
    export default class TextInput extends Vue {
        @Prop()
        public value?: string;

        @Prop([Boolean])
        public short!: Boolean;

        @Prop([Boolean])
        public textarea!: Boolean;

        @Prop()
        public rows?: string;

        get className(): string {
            return this.short.valueOf() ? "textField" : "textField longField";
        }
    }
</script>

<style scoped>

</style>
