package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Map;

public class SamlLoginPageExtension extends SimplePageExtension {

    @NotNull
    private final SamlPluginSettingsStorage settingsStorage;
    private static final String PAGE_EXTENSION_ID = "SamlLogin";
    private LoginConfiguration loginConfiguration;

    public SamlLoginPageExtension(@NotNull PagePlaces pagePlaces,
                                  @NotNull PluginDescriptor descriptor,
                                  @NotNull final SamlPluginSettingsStorage settingsStorage,
                                  @NotNull LoginConfiguration loginConfiguration) {
        super(pagePlaces,
                PlaceId.LOGIN_PAGE,
                PAGE_EXTENSION_ID,
                descriptor.getPluginResourcesPath("SamlLogin.jsp"));
        this.loginConfiguration = loginConfiguration;
        register();
        this.settingsStorage = settingsStorage;
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return loginConfiguration.isAuthModuleConfigured(SamlAuthenticationScheme.class);
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        super.fillModel(model, request);
        SamlPluginSettings samlSettings;
        try {
            samlSettings = settingsStorage.load();
        } catch (IOException e) {
            samlSettings = new SamlPluginSettings();
        }
        model.put("samlSettings", samlSettings);
    }
}
