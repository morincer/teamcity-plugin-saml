<%@ page import="jetbrains.buildServer.auth.saml.plugin.SamlPluginConstants" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="samlSettings" scope="request" type="jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings"/>
<jsp:useBean id="loginUrl" scope="request" type="java.lang.String"/>

<c:if test="${samlSettings.hideLoginForm}">
    <style>
        .loginForm {
            display: none;
        }
    </style>
</c:if>

<div class="buttons">
    <a href="<%=loginUrl%>" class="btn btn_primary">${samlSettings.ssoLoginButtonName}</a>
</div>
