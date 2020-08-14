package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import jetbrains.buildServer.serverSide.auth.AuthModuleType;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.util.WebUtil;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class SamlLoginPageExtension extends SimplePageExtension {

    @NotNull
    private final SamlPluginSettingsStorage settingsStorage;
    private static final String PAGE_EXTENSION_ID = "SamlLogin";
    private LoginConfiguration loginConfiguration;
    private RootUrlHolder rootUrlHolder;
    private SamlAuthenticationScheme scheme;

    public SamlLoginPageExtension(@NotNull PagePlaces pagePlaces,
                                  @NotNull PluginDescriptor descriptor,
                                  @NotNull RootUrlHolder rootUrlHolder,
                                  @NotNull final SamlPluginSettingsStorage settingsStorage,
                                  @NotNull SamlAuthenticationScheme scheme) {
        super(pagePlaces,
                PlaceId.LOGIN_PAGE,
                PAGE_EXTENSION_ID,
                descriptor.getPluginResourcesPath("SamlLogin.jsp"));
        this.rootUrlHolder = rootUrlHolder;
        this.scheme = scheme;
        register();
        this.settingsStorage = settingsStorage;
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return scheme.isConfigured();
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
        model.put("loginUrl", getLoginUrl());
    }

    public String getLoginUrl() {
        String result = WebUtil.combineContextPath(rootUrlHolder.getRootUrl(), SamlPluginConstants.SAML_INITIATE_LOGIN_URL.replace("**", ""));
        if (result.startsWith("/")) {
            result = result.substring(1);
        }

        return result;
    }
}
