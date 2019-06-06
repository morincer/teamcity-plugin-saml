package org.gromozeka.teamcity.saml.plugin;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.gromozeka.teamcity.saml.core.config.SamlPluginSettings;
import org.gromozeka.teamcity.saml.core.config.SamlPluginSettingsStorage;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class SamlPluginAdminPage extends AdminPage {
    private SamlPluginSettings pluginSettings;
    private SamlPluginSettingsStorage settingsStorage;

    protected SamlPluginAdminPage(@NotNull PagePlaces pagePlaces,
                                  @NotNull PluginDescriptor pluginDescriptor,
                                  @NotNull SamlPluginSettingsStorage settingsStorage) {
        super(pagePlaces);
        this.settingsStorage = settingsStorage;


        this.pluginSettings = pluginSettings;
        setPluginName(SamlPluginConstants.PLUGIN_NAME);
        setIncludeUrl(pluginDescriptor.getPluginResourcesPath("SamlPluginAdminPage.jsp"));
        setTabTitle("SAML Settings");
        register();
        Loggers.SERVER.info("SAML configuration page registered");
    }

    @NotNull
    @Override
    public String getGroup() {
        return USER_MANAGEMENT_GROUP;
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        super.fillModel(model, request);
        try {
            model.put("settings", this.settingsStorage.load());
        } catch (IOException e) {
            Loggers.SERVER.error("Failed to load the settings from the storage: " + e.getMessage(), e);
            model.put("settings", new SamlPluginSettings());
        }
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS);
    }
}
