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
    import Vue from "vue";
    import Component from "vue-class-component";
    import {Prop} from "vue-property-decorator";
    import {ApiError} from "@/services/ISettingsApiService";

    @Component({})
    export default class MessagesBox extends Vue {
        @Prop()
        errors: ApiError[] = [];

        @Prop()
        successMessage: string = "";

        get hasErrors(): boolean {
            var result = this.errors != undefined && this.errors.length > 0;
            console.log("hasErrors: ", result);
            return result;
        }
    }
</script>

<style scoped>

</style>
