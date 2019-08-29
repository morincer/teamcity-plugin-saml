package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.controllers.json.BaseJsonController;
import jetbrains.buildServer.controllers.json.JsonActionError;
import jetbrains.buildServer.controllers.json.JsonActionResult;
import jetbrains.buildServer.controllers.json.JsonControllerAction;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.StringUtils;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validation;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Collectors;

public class SamlSettingsJsonController extends BaseJsonController {

    private SamlPluginSettingsStorage settingsStorage;
    private RootUrlHolder rootUrlHolder;

    protected SamlSettingsJsonController(@NotNull SamlPluginSettingsStorage settingsStorage,
                                         WebControllerManager controllerManager,
                                         RootUrlHolder rootUrlHolder) {
        super("/admin/samlSettingsApi.html", controllerManager);
        this.settingsStorage = settingsStorage;
        this.rootUrlHolder = rootUrlHolder;

        registerAction(JsonControllerAction.forParam("action", "get").using(HttpMethod.GET).run(this::getSettings));
        registerAction(JsonControllerAction.forParam("action", "save").using(HttpMethod.POST).run(this::saveSettings));
    }

    public JsonActionResult<String> saveSettings(HttpServletRequest request) {
        try {
            var settings = bindFromRequest(request, SamlPluginSettings.class);

            var validator = Validation.buildDefaultValidatorFactory().getValidator();

            var constraintViolations = validator.validate(settings);

            var errors = constraintViolations.stream().map(cv -> new JsonActionError(cv.getMessage())).collect(Collectors.toList());

            if (settings.isCreateUsersAutomatically() && settings.isLimitToPostfixes() && StringUtil.isEmpty(settings.getAllowedPostfixes())) {
                errors.add(new JsonActionError("You must specify allowed postfixes"));
            }

            if (settings.isCreateUsersAutomatically()
                    && settings.getEmailAttributeMapping().getMappingType().equals(SamlAttributeMappingSettings.TYPE_OTHER)
                    && StringUtil.isEmpty(settings.getEmailAttributeMapping().getCustomAttributeName())) {
                errors.add(new JsonActionError("You must specify non-empty attribute name for the e-mail attribute mapping"));
            }

            if (settings.isCreateUsersAutomatically()
                    && settings.getNameAttributeMapping().getMappingType().equals(SamlAttributeMappingSettings.TYPE_OTHER)
                    && StringUtil.isEmpty(settings.getNameAttributeMapping().getCustomAttributeName())) {
                errors.add(new JsonActionError("You must specify non-empty attribute name for the full name attribute mapping"));
            }

            if (errors.size() > 0) {
                return JsonActionResult.fail(errors);
            }

            settingsStorage.save(settings);
            return JsonActionResult.ok(settings);

        } catch (Exception e) {
            return JsonActionResult.fail(e);
        }
    }

    public JsonActionResult<SamlPluginSettings> getSettings(HttpServletRequest request) {
        try {
            var samlPluginSettings = settingsStorage.load();
            String rootUrl = this.rootUrlHolder.getRootUrl();
            var audienceUrl = new URL(new URL(rootUrl), SamlPluginConstants.SAML_CALLBACK_URL.replace("**", ""));
            samlPluginSettings.setSsoCallbackUrl(audienceUrl.toString());
            return JsonActionResult.ok(samlPluginSettings);
        } catch (IOException e) {
            return JsonActionResult.fail(e);
        }
    }
}
