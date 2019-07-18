package jetbrains.buildServer.controllers.json;

import jetbrains.buildServer.controllers.BaseActionController;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class BaseJsonController extends BaseActionController {

    private final WebControllerManager controllerManager;

    protected BaseJsonController(String baseUrl, WebControllerManager controllerManager) {
        super(controllerManager);

        this.controllerManager = controllerManager;
        controllerManager.registerController(baseUrl, this);
    }

    protected void registerAction(JsonControllerAction controllerAction) {
        this.controllerManager.registerAction(this, controllerAction);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        doAction(request, response, null);

        return null;
    }
}
