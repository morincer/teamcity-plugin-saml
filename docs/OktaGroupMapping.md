# Mapping Okta IdP Groups to TeamCity using SAML Authentication

This step-by-step guide outlines the setup required in order to manage TeamCity group membership using Okta group assigment.

## Pre-requisites

* You have completed the initial setup for using Okta IdP with TeamCity as defined in the Okta Setup [guide](OktaSetup.md)

## Step 1. Add groups SAML attribute to Okta app

Login to the Okta developer console and naviate to the TeamCity application created in the pre-requisite steps.

Edit the integration, and on the `Configure SAML` screen, populate the `Group Attribute Statements` section:

![Okta Group Attributes](img/okta_group_mappings.png)

Select the appropriate `Filter` that matches your requirements. If you want _all_ the assigned Okta groups to be passed through to TeamCity, then select the `Matches regex` filter and add the following regex: `\w+`.

Save the new application configuration.

## Step 2. Associate users to required groups

Create any Okta groups that are required, and map users to the new group.

## Step 3. Create the corresponding TeamCity groups.

Create a new group in TeamCity to map the corresponding Okta group to.

![TeamCity new group](img/teamcity_new_group.png)

The `Name` value can be whatever value you want to use, as the group matching is performed on the `Key` value.
Therefore, ensure that the Okta group name and the TeamCity `key` match.
**N.B:** The group matching is case insensitive, so accepting the default all uppercase TeamCity format is fine.

## Step 4. Test

If already logged in to TeamCity, logout and then press the `Login with SSO` button.

Once logged in, confirm group membership by navigating to `My Settings & Tools` > `Groups`.

![TeamCity group membership](img/teamcity_assigned_groups.png)