package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

public class SamlLoginPageExtension extends SimplePageExtension {

    private static final String PAGE_EXTENSION_ID = "SamlLogin";

    public SamlLoginPageExtension(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor) {
        super(pagePlaces,
                PlaceId.LOGIN_PAGE,
                PAGE_EXTENSION_ID,
                descriptor.getPluginResourcesPath("SamlLogin.jsp"));
        register();
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return true;
    }
}
