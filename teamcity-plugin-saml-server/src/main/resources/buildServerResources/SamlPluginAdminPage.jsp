<%@ page import="org.gromozeka.teamcity.saml.plugin.SamlPluginConstants" %>
<%@ page import="java.net.URL" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="forms" uri="http://www.springframework.org/tags/form" %>

<jsp:useBean id="settings" scope="request" type="org.gromozeka.teamcity.saml.core.config.SamlPluginSettings"/>

<%
    URL requestUrl = new URL(request.getRequestURL().toString());
    URL audienceUrl = new URL(requestUrl, SamlPluginConstants.SAML_CALLBACK_URL.replace("**", ""));
    if (settings.getEntityId() == null) {
        URL entityIdUrl = new URL(requestUrl, SamlPluginConstants.SAML_DEFAULT_SP_ENTITY_ID);
        settings.setEntityId(entityIdUrl.toString());
    }
%>

<div id="settingsContainer">
    <form action="<%=SamlPluginConstants.SETTINGS_CONTROLLER_PATH%>" id="sandboxAdminForm" method="post">
        <table class="runnerFormTable">
            <tr class="groupingTitle">
                <td colspan="2">Identity Provider Configuration</td>
            </tr>
            <tr>
                <th>
                    <label for="ssoEndpoint">Single Sign-On URL</label>
                </th>
                <td>
                    <forms:input id="ssoEndpoint" path="settings.ssoEndpoint" cssStyle="width: 300px"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="issuerUrl">Issuer URL</label>
                </th>
                <td>
                    <forms:input id="issuerUrl" path="settings.issuerUrl" cssStyle="width: 300px"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="publicCertificate">Certificate</label>
                </th>
                <td>
                    <forms:textarea id="publicCertificate" path="settings.publicCertificate" cssStyle="width: 300px"/>
                </td>
            </tr>
            <tr class="groupingTitle">
                <td colspan="2">Service Provider Configuration</td>
            </tr>
            <tr>
                <th>
                    <label for="entityId">Entity ID (Audience)</label>
                </th>
                <td>
                    <forms:input id="entityId" path="settings.entityId" cssStyle="width: 300px"/>
                </td>
            </tr>
            <tr>
                <th>
                    <label for="recepient">Single Sign-On URL (Recepient)</label>
                </th>
                <td>
                    <span id="recepient"><%=audienceUrl%></span>
                </td>
            </tr>

        </table>
        <div class="saveButtonsBlock">
            <f:submit label="Save" />
            <f:saving/>
        </div>
    </form>
</div>