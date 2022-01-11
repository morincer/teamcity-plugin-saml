package jetbrains.buildServer.auth.saml.plugin;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.auth.saml.plugin.pojo.MetadataImport;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlAttributeMappingSettings;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettingsResponse;
import jetbrains.buildServer.auth.saml.plugin.utils.SpelExpressionExecutor;
import jetbrains.buildServer.controllers.json.BaseJsonController;
import jetbrains.buildServer.controllers.json.JsonActionError;
import jetbrains.buildServer.controllers.json.JsonActionResult;
import jetbrains.buildServer.controllers.json.JsonControllerAction;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.jetbrains.annotations.NotNull;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validation;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SamlSettingsJsonController extends BaseJsonController {

    private static final Logger log = Loggers.AUTH;

    private final SamlAuthenticationScheme samlAuthenticationScheme;
    private final SamlPluginSettingsStorage settingsStorage;
    private final SamlPluginPermissionsManager permissionsManager;

    protected SamlSettingsJsonController(
            @NotNull SamlAuthenticationScheme samlAuthenticationScheme,
            @NotNull SamlPluginSettingsStorage settingsStorage,
            @NotNull SamlPluginPermissionsManager permissionsManager,
            WebControllerManager controllerManager) {
        super("/admin/samlSettingsApi.html", controllerManager);
        this.samlAuthenticationScheme = samlAuthenticationScheme;
        this.settingsStorage = settingsStorage;
        this.permissionsManager = permissionsManager;

        registerAction(JsonControllerAction.forParam("action", "get").using(HttpMethod.GET).run(this::getSettings));
        registerAction(JsonControllerAction.forParam("action", "save").using(HttpMethod.POST).run(this::saveSettings));
        registerAction(JsonControllerAction.forParam("action", "import").using(HttpMethod.POST).run(this::importMetadata));
    }

    @NotNull
    public JsonActionResult<SamlPluginSettings> importMetadata(HttpServletRequest request) {
        if (!permissionsManager.canWriteSettings(request)) {
            return JsonActionResult.forbidden();
        }

        try {

            var metadataImport = bindFromRequest(request, MetadataImport.class);

            if (metadataImport == null) {
                throw new Exception("Invalid request");
            }

            var metadataXml = metadataImport.getMetadataXml();

            var getSettingsResult = this.getSettings(request);

            if (getSettingsResult.getErrors() != null) {
                return JsonActionResult.fail(getSettingsResult.getErrors());
            }

            var result = getSettingsResult.getResult();
            SamlPluginSettings settings = result.getSettings();

            this.samlAuthenticationScheme.importMetadataIntoSettings(metadataXml, settings);
            return JsonActionResult.ok(settings);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return JsonActionResult.fail(e);
        }
    }

    public JsonActionResult<SamlPluginSettings> saveSettings(HttpServletRequest request) {
        if (!permissionsManager.canWriteSettings(request)) {
            return JsonActionResult.forbidden();
        }

        try {
            var settings = bindFromRequest(request, SamlPluginSettings.class);

            var validator = Validation.buildDefaultValidatorFactory().getValidator();

            var constraintViolations = validator.validate(settings);

            var errors = constraintViolations.stream().map(cv -> new JsonActionError(cv.getMessage())).collect(Collectors.toList());

            if (settings.isCreateUsersAutomatically() && settings.isLimitToPostfixes() && StringUtil.isEmpty(settings.getAllowedPostfixes())) {
                errors.add(new JsonActionError("You must specify allowed postfixes"));
            }

            if (settings.isCreateUsersAutomatically()) {
                validateAttributeMapping(settings.getNameAttributeMapping(), "name", errors);
                validateAttributeMapping(settings.getEmailAttributeMapping(), "email", errors);
            }

            if (settings.isAssignGroups()) {
                validateAttributeMapping(settings.getGroupsAttributeMapping(), "groups", errors);
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

    public static void validateAttributeMapping(SamlAttributeMappingSettings mapping, String attributeName, List<JsonActionError> errors) {
        if (mapping.getMappingType().equals(SamlAttributeMappingSettings.TYPE_OTHER)) {
            if (StringUtil.isEmpty(mapping.getCustomAttributeName())) {
                errors.add(new JsonActionError(String.format("You must specify non-empty attribute name for the %s attribute mapping", attributeName)));
            }
        } else if (mapping.getMappingType().equals(SamlAttributeMappingSettings.TYPE_EXPRESSION)) {
            if (StringUtil.isEmpty(mapping.getCustomAttributeName())) {
                errors.add(new JsonActionError(String.format("You must specify non-empty expression for the %s attribute mapping", attributeName)));
            } else {
                String expression = mapping.getCustomAttributeName();
                var expressionExecutor = new SpelExpressionExecutor();
                var errorMessage = expressionExecutor.validate(expression);
                if (errorMessage != null) {
                    errors.add(new JsonActionError(String.format("Syntax error in expression %s :%s", expression, errorMessage)));
                }
            }
        }
    }

    public JsonActionResult<SamlPluginSettingsResponse> getSettings(HttpServletRequest request) {

        if (!permissionsManager.canReadSettings(request)) {
            return JsonActionResult.forbidden();
        }

        try {
            var samlPluginSettings = settingsStorage.load();
            var callbackUrl = this.samlAuthenticationScheme.getCallbackUrl();
            samlPluginSettings.setSsoCallbackUrl(callbackUrl.toString());

            if (StringUtil.isEmpty(samlPluginSettings.getEntityId())) {
                samlPluginSettings.setEntityId(callbackUrl.toString());
            }

            var response = new SamlPluginSettingsResponse();
            response.setSettings(samlPluginSettings);
            response.setReadonly(!permissionsManager.canWriteSettings(request));

            if (request != null && request.getSession() != null) {
                response.setCsrfToken(request.getSession().getAttribute("tc-csrf-token").toString());
            }

            return JsonActionResult.ok(response);
        } catch (IOException e) {
            return JsonActionResult.fail(e);
        }
    }


}
