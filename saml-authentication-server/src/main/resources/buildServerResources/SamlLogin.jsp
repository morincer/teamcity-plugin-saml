<%@ page import="jetbrains.buildServer.auth.saml.plugin.SamlPluginConstants" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<c:if test="${samlSettings.hideLoginForm}">
    <style>
        .loginForm {
            display: none;
        }
    </style>
</c:if>

<div class="buttons">
    <a href="<%=SamlPluginConstants.SAML_INITIATE_LOGIN_URL.replace("**", "")%>" class="btn btn_primary">Login with SSO</a>
</div>
