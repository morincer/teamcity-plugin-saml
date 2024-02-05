<template>
  <div>

    <MessagesBox :errors="errors" :successMessage="successMsg"/>

    <RunnerForm>
      <RunnerFormRow>
        <template v-slot:label>
          <router-link to="/new" :disabled="isReadonly" tag="button" class="btn btn_primary submitButton"
                       style="margin-bottom: 10px">Connect
            Provider
          </router-link>
        </template>
        <template v-slot:note>
          Allows you to easily configure connection to one of the pre-defined SSO providers.
          Currently, Okta and Onelogin providers are supported
        </template>
      </RunnerFormRow>
      <GroupingHeader>Identity Provider Configuration</GroupingHeader>
      <RunnerFormRow>
        <template v-slot:label>Single Sign-on URL <span class="mandatoryAsterix">&nbsp;*</span></template>
        <template v-slot:content>
          <TextInput v-model="settings.ssoEndpoint"/>
          <router-link to="/import" tag="button" class="btn btn_primary submitButton"
                       :disabled="isLoading || isSaving || isReadonly "
                       style="margin-left: 10px">Import IdP Metadata
          </router-link>
        </template>
      </RunnerFormRow>
      <RunnerFormInput label="Issuer URL (Identity Provider Entity Id)" required v-model="settings.issuerUrl"/>
      <RunnerFormInput label="X509 Certificate" textarea required v-model="settings.publicCertificate"/>
      <RunnerFormRow>
        <template v-slot:label>Additional Certificates</template>
        <template v-slot:content>
          <div v-for="(cert, key) in additionalCerts" style="margin-bottom: 10px" :key="key">
            <TextInput v-model="cert.value" textarea class="input_certificate"/>
            <input type="button" class="btn" value="Remove" style="margin-left: 10px; vertical-align: top"
                   :disabled="isReadonly"
                   @click="removeAdditionalCertificate(key)">
          </div>
          <div>
            <input type="submit" value="Add" class="btn submitButton" :disabled="isReadonly"
                   @click="addAdditionalCertificate()"/>
          </div>
        </template>
      </RunnerFormRow>

      <GroupingHeader>Service Provider Configuration</GroupingHeader>
      <RunnerFormInput label="Entity ID (Audience)" required v-model="settings.entityId"
                       note="Could be any valid URN - by default it matches the callback URL"/>
      <RunnerFormRow>
        <template v-slot:label>Single Sign-On URL (Recipient)</template>
        <template v-slot:content>{{ settings.ssoCallbackUrl }}</template>
      </RunnerFormRow>

      <RunnerFormRow>
        <template v-slot:label>SP Metadata XML</template>
        <template v-slot:content><a href="../app/saml/metadata.xml" target="_blank">Download</a></template>
        <template v-slot:note>(Reminder) You must save your settings first, so the latest changes become available in the
          metadata XML
        </template>
      </RunnerFormRow>

      <GroupingHeader>Automatic Users Creation</GroupingHeader>
      <RunnerFormRow>
        <template v-slot:label>Create users automatically</template>
        <template v-slot:content><input type="checkbox" v-model="settings.createUsersAutomatically"></template>
        <template v-slot:note>If a user name is missing a new user with this name will be created</template>
      </RunnerFormRow>
      <RunnerFormRow v-if="settings.createUsersAutomatically">
        <template v-slot:label>Limit to specific username postfixes</template>
        <template v-slot:content>
          <input type="checkbox" v-model="settings.limitToPostfixes">
          <TextInput style="margin-left: 10px" v-model="settings.allowedPostfixes" v-if="settings.limitToPostfixes"/>
        </template>
        <template v-slot:note>Users will only be created if their names end with particular postfixes - for example,
          @mymail.com (provide comma-separated list for multiple postfixes)
        </template>
      </RunnerFormRow>
      <RunnerFormRow v-if="settings.createUsersAutomatically">
        <template v-slot:label>Assign matching TeamCity groups automatically</template>
        <template v-slot:content>
          <input type="checkbox" v-model="settings.assignGroups">
        </template>
        <template v-slot:note>If a user has a group assigned to their Okta profile that matches an existing TeamCity
          group, then the user will automatically be added to the group in TeamCity. Matching is performed on the group
          Key value, and is case insensitive
        </template>
      </RunnerFormRow>
      <RunnerFormRow v-if="settings.createUsersAutomatically">
        <template v-slot:label>Remove user from unassigned groups automatically</template>
        <template v-slot:content>
          <input type="checkbox" v-model="settings.removeUnassignedGroups">
        </template>
        <template v-slot:note>If a user has a group unassigned from their Okta profile, then remove the corresponding
          group membership in TeamCity.
        </template>
      </RunnerFormRow>

      <RunnerFormRow v-if="settings.createUsersAutomatically">
        <template v-slot:label>Map E-mail From</template>
        <template v-slot:content>
          <SamlAttributeSelect v-model="settings.emailAttributeMapping"/>
        </template>
      </RunnerFormRow>
      <RunnerFormRow v-if="settings.createUsersAutomatically">
        <template v-slot:label>Map Full Name From</template>
        <template v-slot:content>
          <SamlAttributeSelect v-model="settings.nameAttributeMapping"/>
        </template>
      </RunnerFormRow>
      <RunnerFormRow v-if="settings.createUsersAutomatically">
        <template v-slot:label>Map VCS Username From</template>
        <template v-slot:content>
          <SamlAttributeSelect v-model="settings.vcsUsernameAttributeMapping"/>
        </template>
      </RunnerFormRow>
      <RunnerFormRow v-if="settings.createUsersAutomatically">
        <template v-slot:label>Map Groups From</template>
        <template v-slot:content>
          <SamlAttributeSelect v-model="settings.groupsAttributeMapping"/>
        </template>
      </RunnerFormRow>

      <GroupingHeader>Misc</GroupingHeader>
      <RunnerFormInput label="Login Button Label" required v-model="settings.ssoLoginButtonName"/>
      <RunnerFormRow>
        <template v-slot:label>SAML Strict Mode</template>
        <template v-slot:content><input type="checkbox" v-model="settings.strict"/></template>
        <template v-slot:note>Runs additional checks on SAML message</template>
      </RunnerFormRow>
      <RunnerFormRow>
        <template v-slot:label>SAML Callback CORS Filter Exception</template>
        <template v-slot:content><input type="checkbox" v-model="settings.samlCorsFilter"/></template>
        <template v-slot:note>Adds CORS filter exception for POST requests sent to the login callback URL</template>
      </RunnerFormRow>
      <RunnerFormRow>
        <template v-slot:label>Hide Login Form</template>
        <template v-slot:content><input type="checkbox" v-model="settings.hideLoginForm"/></template>
      </RunnerFormRow>

      <RunnerFormRow>
        <template v-slot:label>Compress SAML Request</template>
        <template v-slot:content><input type="checkbox" v-model="settings.compressRequest"></template>
        <template v-slot:note>When making an initial login redirect, the SAMLRequest variable in the URL will be
          compressed prior to Base64 encoding
        </template>
      </RunnerFormRow>

      <template v-slot:actions>
        <input type="submit" value="Save" class="btn btn_primary submitButton"
               :disabled="isLoading || isSaving || isReadonly"
               @click="submit()"/>
        <ProgressIndicator title="Fetching SSO configuration..." v-if="isLoading"/>
        <ProgressIndicator title="Saving..." v-if="isSaving"/>
      </template>
    </RunnerForm>
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import RunnerForm from "@/components/RunnerForm.vue";
import GroupingHeader from "@/components/GroupingHeader.vue";
import RunnerFormRow from "@/components/RunnerFormRow.vue";
import RunnerFormInput from "@/components/RunnerFormInput.vue";
import {Component} from "vue-property-decorator";
import {ApiError, ISettingsApiService, SamlSettings} from "@/services/ISettingsApiService";
import TextInput from "@/components/TextInput.vue";
import ProgressIndicator from "@/components/ProgressIndicator.vue";
import MessagesBox from "@/components/MessagesBox.vue";
import {appConfig} from "@/main.dependencies";
import SamlAttributeSelect from "@/components/SamlAttributeSelect.vue";

interface IValue {
  value: string;
}

@Component({
  components: {
    SamlAttributeSelect,
    MessagesBox,
    TextInput, RunnerFormInput, RunnerFormRow, GroupingHeader, RunnerForm, ProgressIndicator,
  },
})
export default class SamlPluginSettings extends Vue {

  public settingsApiService: ISettingsApiService = appConfig.settingsApiService!;

  public settings: SamlSettings = {additionalCerts: []};
  public isLoading: boolean = false;
  public isSaving: boolean = false;
  public isReadonly: boolean = false;
  public errors: ApiError[] = [];
  public successMsg: string = "";
  public additionalCerts: { [key: string]: IValue } = {};

  public async mounted() {
    try {
      this.isLoading = true;
      const result = await this.settingsApiService.get();

      if (result.result) {
        this.settings = result.result.settings;
        this.additionalCerts = {};
        this.isReadonly = result.result.readonly;
        if (this.settings.additionalCerts) {
          this.settings.additionalCerts.forEach((c) => this.addAdditionalCertificate(c));
        }
      }
    } catch (e) {
      this.errors = [{message: (e as Error).message, code: 0}];
    } finally {
      this.isLoading = false;
    }
  }

  public async submit() {
    this.isSaving = true;
    this.errors = [];
    this.successMsg = "";

    if (this.additionalCerts) {
      this.settings.additionalCerts = [];
      Object.keys(this.additionalCerts)
          .forEach((key) => this.settings.additionalCerts?.push(this.additionalCerts[key].value));
    }

    const result = await this.settingsApiService.save(this.settings);
    this.isSaving = false;
    if (result.result) {
      this.successMsg = "Your changes have been saved.";
    } else if (result.errors) {
      this.errors = result.errors;
    }
  }

  public addAdditionalCertificate(c: string = "") {
    const key = "cert_" + new Date().getTime() + "_" + Math.random();
    Vue.set(this.additionalCerts, key, {value: c});
  }

  public removeAdditionalCertificate(key: string) {
    Vue.delete(this.additionalCerts, key);
  }


}
</script>

<style scoped>

</style>
