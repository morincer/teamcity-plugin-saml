package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class SamlPluginConfiguration {

    @Bean
    SamlAuthenticationScheme samlAuthenticationScheme(LoginConfiguration loginConfiguration, SamlPluginSettingsStorage samlPluginSettingsStorage, UserModel userModel) {
        return new SamlAuthenticationScheme(loginConfiguration, samlPluginSettingsStorage, userModel);
    }

    @Bean
    SamlLoginPageExtension samlLoginPageExtension(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor, SamlPluginSettingsStorage settingsStorage) {
        return new SamlLoginPageExtension(pagePlaces, descriptor, settingsStorage);
    }

    @Bean
    SamlLoginController samlLoginController(SBuildServer server, WebControllerManager webControllerManager, AuthorizationInterceptor interceptor, SamlPluginSettingsStorage settingsStorage) {
        return new SamlLoginController(server, webControllerManager, interceptor, settingsStorage);
    }

    @Bean
    SamlCallbackController samlCallbackController(SBuildServer server, WebControllerManager webControllerManager, AuthorizationInterceptor interceptor) {
        return new SamlCallbackController(server, webControllerManager);
    }

    @Bean
    SamlPluginAdminPage samlPluginAdminPage(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor, SamlPluginSettingsStorage settingsStorage) {
        return new SamlPluginAdminPage(pagePlaces, descriptor, settingsStorage);
    }

    @Bean
    SamlPluginSettingsStorage samlPluginSettingsStorage(ServerPaths serverPaths) throws IOException {
        var samlPluginSettingsStorage = new SamlPluginSettingsStorage(serverPaths);
        samlPluginSettingsStorage.init();
        return samlPluginSettingsStorage;
    }

    @Bean
    SamlPluginAdminPageController samlPluginAdminPageController(SamlPluginSettingsStorage settingsStorage, WebControllerManager webControllerManager) {
        return new SamlPluginAdminPageController(settingsStorage, webControllerManager);
    }

    @Bean
    SamlSettingsJsonController samlSettingsAjaxController(WebControllerManager controllerManager) throws IOException {
        return new SamlSettingsJsonController(samlPluginSettingsStorage(null), controllerManager);
    }
}
