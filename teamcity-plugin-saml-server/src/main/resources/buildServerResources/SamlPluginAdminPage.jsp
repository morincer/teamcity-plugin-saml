<%@ c:taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url value="/saml/adminSettings.html" var="actionUrl"/>

<div id="settingsContainer">
    <form action="${actionUrl}" id="samlSettingsAdminForm" method="post">
        <table class="runnerFormTable">
            <tr class="groupingTitle">
                <td colspan="2">General Configuration&nbsp;<a href="http://www.quali.com/" class="helpIcon" style="vertical-align: middle;" target="_blank"><bs:helpIcon/></a></td>
            </tr>
            <tr>
                <th>
                    <label for="serverAddress">Sandbox API Host Address: <l:star /></label>
                </th>
                <td>
                    <forms:textField name="serverAddress" value="${sandboxSettings.serverAddress}" style="width: 300px;" />
                    <span class="smallNote">CloudShell Sandbox API address and port. By default, the Sandbox API is using port 82.
                                                For Example: http://192.168.1.1:82 or https://10.10.19.1:82</span>
                    <span class="error" id="errorServerAddress"></span>
                </td>
            </tr>
        </table>
    </form>
</div>