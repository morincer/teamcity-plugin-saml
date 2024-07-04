# How to configure TeamCity SAML plugin for Azure AD (Microsoft Azure Active Directory)

## General configuration

1. Add an Enterprise Application in Azure Portal. **Enterprise Applications** > **+ New application** > **+ Create your own application** > set name > 	**Integrate any other application you don't find in the gallery (Non-gallery)** > **Create**.
1. Go to **Enterprise Apps** > your app.
1. On the **Properties** pane, make sure that **Enabled for users to sign-in?** is set to **Yes**.
1. On the **Users and groups** pane, click **+ Add user/group** and add people who can use the app.
1. On the **Single Sign-On** pane, click **SAML**.
1. Next to **Basic SAML Configuration**, click **Edit**.
1. Set **Identifier (Entity ID)** and **Reply URL (Assertion Consumer Service URL)** to `https://<teamcity>/app/saml/callback/`. **Save**.
1. Click **Download** In the **SAML Certificates** section > **Federation Metadata XML**.
1. Go to TeamCity SAML plugin settings.
1. Click **Import IdP Metadata**, paste the content of the downloaded Federation Metadata XML file, click **Import** and then **Save**.
1. Set **Entity ID (Audience)** to `https://<teamcity>/app/saml/callback/` and click **Save**.
1. Tick **Create users automatically**.
1. Go to Azure Portal > **Enterprise Apps** > your app.
1. On **Single sign-on** pane, click **Test**. You must be able to sign into TeamCity. If not, troubleshoot the error given by the Azure Portal.

Now you can log into TeamCity using Azure AD, but TeamCity does not sync the user attributes from Azure AD. To enable that, set up the attribute sync.

## Attributes Sync

1. Copy the Object Id (not name) of the group in Azure Portal and a group with the same Group Key in TeamCity (group names do not have to be the same).
1. Go to **Enterprise Apps** > your app.
1. On **Single Sign-On** pane, next to **Attributes & Claims**, click **Edit**.
1. Click **+ Add a group claim**.
1. Select **All groups**, set **Source attribute** to **Group ID**. **Save**.
1. Copy the **Claim name** field of the email and user groups (e.g., `http://schemas.microsoft.com/ws/2008/06/identity/claims/groups`, `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress`).
1. Go to TeamCity SAML plugin settings.
1. Tick **Assign matching TeamCity groups automatically**, **Remove user from unassigned groups automatically**.
1. Set **Map E-mail From** to **Custom Attribute** with value copied from Azure Portal, e.g. `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress`.
1. Set **Map E-mail From** to **Map Groups From** with value copied from Azure Portal, e.g. `http://schemas.microsoft.com/ws/2008/06/identity/claims/groups`.
1. **Save** SAML settings.

Now the groups specified in step 1 should be synched. If they are not, enable the `debug-auth` [logging preset](https://www.jetbrains.com/help/teamcity/teamcity-monitoring-and-diagnostics.html#Logging+Presets) and see the SAML Groups in `teamcity-saml.log` to troubleshoot the issue.

## Changing Username Format

The plugin gets the username from the `NameIdentity` claim of the SAML response sent by Azure AD. If it is in the `username@domain` format you might want to drop the `@domain` part and only leave the `username` part. Here's how you can do that:

1. Edit the Attribute & Claims section of the SAML settings of your Enterprise Application in Azure Portal.
1. Edit the **Unique User Identifier (Name ID)** claim.
1. Set **Name identifier format** to `Email address`.
1. Set **Source** to `Transformation`.
1. Under **Manage transformation**, set **Transformation** to `ExtractMailPrefix()` and set **Parameter 1** to `user.mail`.
1. Save everything.
