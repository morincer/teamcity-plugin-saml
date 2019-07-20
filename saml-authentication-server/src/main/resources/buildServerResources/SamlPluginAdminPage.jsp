<%@ page import="jetbrains.buildServer.auth.saml.plugin.SamlPluginConstants" %>
<%@ page import="java.net.URL" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="f" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="forms" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="settings" scope="request" type="jetbrains.buildServer.auth.saml.plugin.SamlPluginSettings"/>
<jsp:useBean id="pluginResources" scope="request" type="java.lang.String"/>
<link href=${pluginResources}/admin-ui/css/app.css rel=preload as=style>
<link href=${pluginResources}/admin-ui/css/chunk-vendors.css rel=preload as=style>
<link href=${pluginResources}/admin-ui/js/app.js rel=preload as=script>
<link href=${pluginResources}/admin-ui/js/chunk-vendors.js rel=preload as=script>
<link href=${pluginResources}/admin-ui/css/chunk-vendors.css rel=stylesheet>
<link href=${pluginResources}/admin-ui/css/app.css rel=stylesheet>
<div id=app></div>
<script src=${pluginResources}/admin-ui/js/chunk-vendors.js></script>
<script src=${pluginResources}/admin-ui/js/app.js></script>

</div>
