package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.controllers.json.BaseJsonController;
import jetbrains.buildServer.controllers.json.JsonActionError;
import jetbrains.buildServer.controllers.json.JsonActionResult;
import jetbrains.buildServer.controllers.json.JsonControllerAction;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import lombok.var;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;

public class SamlSettingsAjaxController extends BaseJsonController {

    protected SamlSettingsAjaxController(WebControllerManager controllerManager) {
        super("/admin/jsonAjax.html", controllerManager);

        registerAction(JsonControllerAction.forParam("action", "show").using(HttpMethod.GET).run(this::show));
        registerAction(JsonControllerAction.forParam("action", "fail").using(HttpMethod.GET).run(this::fail));
    }

    private JsonActionResult<?> fail(HttpServletRequest request) {
        return JsonActionResult.fail(new JsonActionError(1, "Some error has happened"));
    }

    private JsonActionResult show(HttpServletRequest request) {
        var result = new SamlPluginSettings();
        result.setEntityId("Blablabla");
        return JsonActionResult.ok(result);
    }
}
