package org.gromozeka.teamcity.saml.plugin;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.gromozeka.teamcity.saml.core.config.SamlPluginSettings;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class SamlPluginAdminPage extends AdminPage {
    private SamlPluginSettings pluginSettings;

    protected SamlPluginAdminPage(@NotNull PagePlaces pagePlaces,
                                  @NotNull PluginDescriptor pluginDescriptor) {
        super(pagePlaces);


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
}
