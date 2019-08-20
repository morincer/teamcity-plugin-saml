<template>
    <div>
        <input type="radio" id="none" v-on:change="emitChange()" :value="SamlAttributeMappingTypes.None" v-model="mappingType"/>
        <label for="none">None</label>

        <input type="radio" id="name_id" v-on:change="emitChange()" :value="SamlAttributeMappingTypes.NameID" v-model="mappingType"/>
        <label for="name_id">Name ID</label>

        <input type="radio" id="custom" v-on:change="emitChange()" :value="SamlAttributeMappingTypes.Other" v-model="mappingType"/>
        <label for="custom">Custom</label>

        <input type="text" v-on:change="emitChange()" v-if="mappingType == SamlAttributeMappingTypes.Other" v-model="customAttributeName"/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Component, Prop} from 'vue-property-decorator';
    import {SamlAttributeMapping, SamlAttributeMappingTypes } from "@/services/ISettingsApiService";

    @Component({})
    export default class SamlAttributeSelect extends Vue {
        @Prop()
        public value!: SamlAttributeMapping;

        public mappingType: string = SamlAttributeMappingTypes.None;
        public customAttributeName: string = "";

        SamlAttributeMappingTypes = SamlAttributeMappingTypes;

        mounted() {
            if (this.value) {
                this.mappingType = this.value.mappingType;
                this.customAttributeName = this.value.customAttributeName!;
            }
        }

        emitChange() {
            const value : SamlAttributeMapping = {
                mappingType: this.mappingType, customAttributeName: this.customAttributeName
            };

            this.$emit('input', value);
        }


    }
</script>

<style scoped>

</style>
