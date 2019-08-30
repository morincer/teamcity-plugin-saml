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
    import Vue from 'vue';
    import {ISettingsApiService, SamlSettings} from "@/services/ISettingsApiService";
    import RunnerForm from "@/components/RunnerForm.vue";
    import RunnerFormRow from "@/components/RunnerFormRow.vue";
    import GroupingHeader from "@/components/GroupingHeader.vue";
    import TextInput from "@/components/TextInput.vue";
    import {appConfig} from "@/main.dependencies";
    import ProgressIndicator from "@/components/ProgressIndicator.vue";

    export interface ParametersGroup {
        message?: string,
        title: string,
        parameters: ParameterDecoration[]
    }

    export interface ParameterDecoration {
        fieldName: string,
        label: string,
        hint: string,
        textarea?: boolean,
        readonly?: boolean,
    }

    export interface SettingsDecoration {
        introMessage: string,
        groups: ParametersGroup[]
    }

    @Component({components: {TextInput, GroupingHeader, RunnerForm, RunnerFormRow, ProgressIndicator}})
    export default class SettingsFacade extends Vue {
        @Prop()
        settingsDecoration?: SettingsDecoration;

        @Prop()
        settings?: SamlSettings;
    }
</script>

<style scoped>

</style>
