<template>
  <div>
    <select v-model="mappingType" v-on:change="emitChange()">
      <option :value="SamlAttributeMappingTypes.None">None</option>
      <option :value="SamlAttributeMappingTypes.NameID">Name ID</option>
      <option :value="SamlAttributeMappingTypes.Other">Custom Attribute</option>
      <option :value="SamlAttributeMappingTypes.Expression">Expression</option>

    </select>
    <!--
    <input type="radio" id="none" v-on:change="emitChange()" :value="SamlAttributeMappingTypes.None" v-model="mappingType"/>
    <label for="none">None</label>

    <input type="radio" id="name_id" v-on:change="emitChange()" :value="SamlAttributeMappingTypes.NameID" v-model="mappingType"/>
    <label for="name_id">Name ID</label>

    <input type="radio" id="custom" v-on:change="emitChange()" :value="SamlAttributeMappingTypes.Other" v-model="mappingType"/>
    <label for="custom">Custom</label>
-->
    <input type="text" style="margin-left: 10px" v-on:change="emitChange()"
           v-if="mappingType == SamlAttributeMappingTypes.Other" v-model="customAttributeName"/>
    <div v-if="mappingType == SamlAttributeMappingTypes.Expression" style="margin-top: 20px">

      <TextInput v-model="customAttributeName"
                 v-on:input="emitChange()"
                 textarea/>

      <span class="smallNote">
        <p>The field uses <a href="https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html">Spring Expression Language (SpEL)</a> syntax with SAML request object attributes mapped as a context</p>
        <p>NameId is accessible by <i>nameid</i>, custom attributes - by their respective names. To reference a custom attribute, use either it's name directly
          (if it matches standard Java variable naming conditions) or utilize <i>get('attribute name')</i> function </p>
        <p><strong>Examples:</strong></p>
        <p>Concatenate first name and second name: <i>firstName + ' ' + secondName</i></p>
        <p>Same using get(): <i>get('firstName') + ' ' + get('secondName')</i></p>

      </span>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import {Component, Prop} from "vue-property-decorator";
import {SamlAttributeMapping, SamlAttributeMappingTypes} from "@/services/ISettingsApiService";
import TextInput from "@/components/TextInput.vue";

@Component({
  components: {TextInput}
})
export default class SamlAttributeSelect extends Vue {
  @Prop()
  public value!: SamlAttributeMapping;

  public mappingType: string = SamlAttributeMappingTypes.None;
  public customAttributeName: string = "";

  public SamlAttributeMappingTypes = SamlAttributeMappingTypes;

  public mounted() {
    if (this.value) {
      this.mappingType = this.value.mappingType;
      this.customAttributeName = this.value.customAttributeName!;
    }
  }

  public emitChange() {
    const value: SamlAttributeMapping = {
      mappingType: this.mappingType, customAttributeName: this.customAttributeName,
    };

    this.$emit("input", value);
  }


}
</script>

<style scoped>

</style>
