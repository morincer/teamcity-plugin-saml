package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.controllers.json.BaseJsonController;
import jetbrains.buildServer.controllers.json.JsonActionError;
import jetbrains.buildServer.controllers.json.JsonActionResult;
import jetbrains.buildServer.controllers.json.JsonControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.net.URL;

public class SamlSettingsJsonController extends BaseJsonController {

    private SamlPluginSettingsStorage settingsStorage;

    protected SamlSettingsJsonController(@NotNull SamlPluginSettingsStorage settingsStorage,
                                         WebControllerManager controllerManager) {
        super("/admin/samlSettingsApi.html", controllerManager);
        this.settingsStorage = settingsStorage;

        registerAction(JsonControllerAction.forParam("action", "get").using(HttpMethod.GET).run(this::getSettings));
        registerAction(JsonControllerAction.forParam("action", "save").using(HttpMethod.POST).run(this::saveSettings));
    }

    private JsonActionResult<String> saveSettings(HttpServletRequest request) {
        try {
            var settings = bindFromRequest(request, SamlPluginSettings.class);

            var validator = Validation.buildDefaultValidatorFactory().getValidator();
            var errors = validator.validate(settings);

            if (errors.size() > 0) {
                return JsonActionResult.fail(errors);
            }

            settingsStorage.save(settings);
            return JsonActionResult.ok(settings);

        } catch (Exception e) {
            return JsonActionResult.fail(e);
        }
    }

    private JsonActionResult<?> getSettings(HttpServletRequest request) {
        try {
            var samlPluginSettings = settingsStorage.load();
            var audienceUrl = new URL(new URL(request.getRequestURL().toString()), SamlPluginConstants.SAML_CALLBACK_URL.replace("**", ""));
            samlPluginSettings.setSsoCallbackUrl(audienceUrl.toString());
            return JsonActionResult.ok(samlPluginSettings);
        } catch (IOException e) {
            return JsonActionResult.fail(e);
        }
    }
}
