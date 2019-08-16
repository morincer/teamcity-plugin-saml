<template>
    <AdminPage>
        <SamlPluginSettings />
    </AdminPage>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import AdminPage from '@/components/AdminPage.vue';
import RunnerForm from '@/components/RunnerForm.vue';
import GroupingHeader from '@/components/GroupingHeader.vue';
import RunnerFormRow from '@/components/RunnerFormRow.vue';
import RunnerFormInput from '@/components/RunnerFormInput.vue';
import SamlPluginSettings from '@/views/SamlPluginSettings.vue';

@Component({
    components: {SamlPluginSettings, RunnerFormInput, RunnerFormRow, GroupingHeader, RunnerForm, AdminPage},
})
export default class AdminDemo extends Vue {
    public myvalue: string = 'This is input value';
}
</script>
