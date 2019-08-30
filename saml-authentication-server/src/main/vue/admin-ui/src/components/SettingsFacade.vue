<template>
    <div>
        <div class="successMessage" v-if="settingsDecoration.introMessage" style="margin-bottom: 10px">{{settingsDecoration.introMessage}}
        </div>
        <RunnerForm v-for="group in settingsDecoration.groups">
            <GroupingHeader>{{group.title}}</GroupingHeader>

            <tr v-if="group.message">
                <td colspan="2">
                    <span class="smallNote">{{group.message}}</span>
                </td>
            </tr>

            <RunnerFormRow v-for="decoration in group.parameters">
                <template v-slot:label>{{decoration.label}}</template>
                <template v-slot:content>
                    <TextInput v-model="settings[decoration.fieldName]" :textarea="decoration.textarea == true" v-if="decoration.readonly !== true"/>
                    <span v-if="decoration.readonly === true">{{settings[decoration.fieldName]}}</span>
                </template>
                <template v-slot:note v-if="decoration.hint">{{decoration.hint}}</template>
            </RunnerFormRow>
        </RunnerForm>
    </div>
</template>

<script lang="ts">
    import {Component, Prop} from "vue-property-decorator";
    import Vue from "vue";
    import {SamlSettings} from "@/services/ISettingsApiService";
    import RunnerForm from "@/components/RunnerForm.vue";
    import RunnerFormRow from "@/components/RunnerFormRow.vue";
    import GroupingHeader from "@/components/GroupingHeader.vue";
    import TextInput from "@/components/TextInput.vue";
    import ProgressIndicator from "@/components/ProgressIndicator.vue";
    import {SettingsDecoration} from "@/resources/ConnectionWizardDecorations";

    @Component({components: {TextInput, GroupingHeader, RunnerForm, RunnerFormRow, ProgressIndicator}})
export default class SettingsFacade extends Vue {
    @Prop()
    public settingsDecoration?: SettingsDecoration;

    @Prop()
    public settings?: SamlSettings;
}
</script>

<style scoped>

</style>
