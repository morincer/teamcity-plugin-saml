<template>
    <div>
        <div class="successMessage" v-if="successMessage">
            {{successMessage}}
        </div>
        <div class="errorMessage" v-if="hasErrors">
            <ul>
                <li v-for="er in errors">{{er.message}}</li>
            </ul>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Component from 'vue-class-component';
    import {Prop} from 'vue-property-decorator';
    import {ApiError} from '@/services/ISettingsApiService';

    @Component({})
export default class MessagesBox extends Vue {
    @Prop()
    public errors: ApiError[] = [];

    @Prop()
    public successMessage: string = '';

    get hasErrors(): boolean {
        return this.errors !== undefined && this.errors.length > 0;
    }
}
</script>

<style scoped>

</style>
