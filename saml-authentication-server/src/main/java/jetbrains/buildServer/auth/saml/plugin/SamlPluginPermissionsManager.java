package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.util.SessionUser;

import javax.servlet.http.HttpServletRequest;

public class SamlPluginPermissionsManager {
    public boolean canReadSettings(HttpServletRequest request) {
        return hasPermission(request, Permission.VIEW_SERVER_SETTINGS) || canWriteSettings(request);
    }

    public boolean canWriteSettings(HttpServletRequest request) {
        if (Permission.lookupPermission("MANAGE_AUTHENTICATION_SETTINGS") != null) {
            //this permission was introduced in TeamCity 2020.1.1, so it may not exist when the server is older than 2020.1.1
            return hasPermission(request, Permission.lookupPermission("MANAGE_AUTHENTICATION_SETTINGS"));
        }
        return hasPermission(request, Permission.CHANGE_SERVER_SETTINGS);
    }

    private boolean hasPermission(HttpServletRequest request, Permission permission) {
        SUser user = SessionUser.getUser(request);
        return user != null && user.isPermissionGrantedGlobally(permission);
    }
}
