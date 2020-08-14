package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.web.SamlCsrfCheck;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class SamlPluginConfiguration {

    @Bean
    SamlAuthenticationScheme samlAuthenticationScheme(LoginConfiguration loginConfiguration, SamlPluginSettingsStorage samlPluginSettingsStorage, UserModel userModel, UserGroupManager userGroupManager, RootUrlHolder rootUrlHolder) {
        SamlAuthenticationScheme samlAuthenticationScheme = new SamlAuthenticationScheme(rootUrlHolder, samlPluginSettingsStorage, userModel, userGroupManager, loginConfiguration);
        loginConfiguration.registerAuthModuleType(samlAuthenticationScheme);

        return samlAuthenticationScheme;
    }

    @Bean
    SamlLoginPageExtension samlLoginPageExtension(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor, @NotNull RootUrlHolder rootUrlHolder, SamlPluginSettingsStorage settingsStorage, SamlAuthenticationScheme scheme) {
        return new SamlLoginPageExtension(pagePlaces, descriptor, rootUrlHolder, settingsStorage, scheme);
    }

    @Bean
    SamlLoginController samlLoginController(SBuildServer server, WebControllerManager webControllerManager, AuthorizationInterceptor interceptor, SamlAuthenticationScheme samlAuthenticationScheme, SamlPluginSettingsStorage settingsStorage) {
        return new SamlLoginController(server, webControllerManager, interceptor, samlAuthenticationScheme, settingsStorage);
    }

    @Bean
    SamlCallbackController samlCallbackController(SBuildServer server, WebControllerManager webControllerManager) {
        return new SamlCallbackController(server, webControllerManager);
    }

    @Bean
    SamlMetadataController samlMetadataController(SBuildServer server, WebControllerManager webControllerManager, AuthorizationInterceptor interceptor, SamlAuthenticationScheme samlAuthenticationScheme, SamlPluginSettingsStorage settingsStorage) {
        return new SamlMetadataController(server, webControllerManager, interceptor, samlAuthenticationScheme, settingsStorage);
    }

    @Bean
    SamlSettingsAdminPage samlPluginAdminPage(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor, SamlPluginSettingsStorage settingsStorage, SamlAuthenticationScheme samlAuthenticationScheme) {
        return new SamlSettingsAdminPage(pagePlaces, descriptor, settingsStorage, samlAuthenticationScheme);
    }

    @Bean
    SamlPluginSettingsStorage samlPluginSettingsStorage(ServerPaths serverPaths) throws IOException {
        var configPath = Paths.get(serverPaths.getConfigDir(), SamlPluginConstants.CONFIG_FILE_NAME);
        var samlPluginSettingsStorage = new SamlPluginSettingsStorageImpl(configPath);
        samlPluginSettingsStorage.init();
        return samlPluginSettingsStorage;
    }

    @Bean
    SamlSettingsJsonController samlSettingsAjaxController(WebControllerManager controllerManager, SamlAuthenticationScheme samlAuthenticationScheme) throws IOException {
        return new SamlSettingsJsonController(samlAuthenticationScheme, samlPluginSettingsStorage(null), controllerManager);
    }

    @Bean
    SamlCsrfCheck samlCsrfCheck(SamlAuthenticationScheme scheme, SamlPluginSettingsStorage settingsStorage) {
        return new SamlCsrfCheck(scheme, settingsStorage);
    }
}
