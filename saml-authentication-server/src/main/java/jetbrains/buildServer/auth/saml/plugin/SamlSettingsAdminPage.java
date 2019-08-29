package jetbrains.buildServer.auth.saml.plugin;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class SamlSettingsAdminPage extends AdminPage {
    private final PluginDescriptor pluginDescriptor;
    private LoginConfiguration loginConfiguration;
    private final Logger LOG = Loggers.SERVER;
    private SamlPluginSettingsStorage settingsStorage;

    protected SamlSettingsAdminPage(@NotNull PagePlaces pagePlaces,
                                    @NotNull PluginDescriptor pluginDescriptor,
                                    @NotNull SamlPluginSettingsStorage settingsStorage,
                                    @NotNull LoginConfiguration loginConfiguration) {
        super(pagePlaces);
        this.settingsStorage = settingsStorage;
        this.pluginDescriptor = pluginDescriptor;
        this.loginConfiguration = loginConfiguration;
        setPluginName(SamlPluginConstants.PLUGIN_NAME);
        setIncludeUrl(pluginDescriptor.getPluginResourcesPath("SamlPluginAdminPage.jsp"));
        setTabTitle("SAML Settings");
        register();
        LOG.info("SAML configuration page registered");
    }

    @NotNull
    @Override
    public String getGroup() {
        return USER_MANAGEMENT_GROUP;
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        super.fillModel(model, request);
        model.put("pluginResources", this.pluginDescriptor.getPluginResourcesPath());

        try {
            model.put("settings", this.settingsStorage.load());
        } catch (IOException e) {
            LOG.error("Failed to load the settings from the storage: " + e.getMessage(), e);
            model.put("settings", new SamlPluginSettings());
        }
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return super.isAvailable(request)
                && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS)
                && loginConfiguration.isAuthModuleConfigured(SamlAuthenticationScheme.class);
    }
}
