package jetbrains.buildServer.controllers.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import jetbrains.buildServer.controllers.GetActionAllowed;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.web.openapi.ControllerAction;
import lombok.Getter;
import lombok.var;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@GetActionAllowed
public class JsonControllerAction implements ControllerAction {

    @Getter
    private String paramName;

    @Getter
    private String paramValue;

    @Getter
    private HttpMethod method = HttpMethod.POST;

    @Getter
    private CheckedFunction<HttpServletRequest, JsonActionResult<?>> handler;

    private JsonControllerAction() {

    }

    public static JsonControllerAction forParam(String paramName, String paramValue) {
        var action = new JsonControllerAction();
        action.paramName = paramName;
        action.paramValue = paramValue;
        return action;
    }

    public JsonControllerAction using(HttpMethod method) {
        this.method = method;
        return this;
    }

    public JsonControllerAction run(CheckedFunction<HttpServletRequest, JsonActionResult<?>> handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public boolean canProcess(@NotNull HttpServletRequest request) {
        boolean canProcess = this.paramName != null && this.paramValue != null
                && this.paramValue.equals(request.getParameter(this.paramName))
                && this.method != null && request.getMethod().equals(this.method.name());
        return canProcess;
    }

    @Override
    public void process(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @Nullable Element ajaxResponse) {
        try {
            if (!canProcess(request)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                JsonActionResult<?> jsonActionResult = this.handler.apply(request);

                if (jsonActionResult.errors != null && jsonActionResult.errors.length > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                }

                response.setContentType("application/json");

                ObjectMapper mapper = new ObjectMapper();
                mapper.writerWithDefaultPrettyPrinter().writeValue(response.getOutputStream(), jsonActionResult);
            }

            response.flushBuffer();
        }
        catch (Exception e) {
            Loggers.SERVER.error(e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write(e.getMessage());
            } catch (IOException ex) {
                Loggers.SERVER.error(ex);
            }
        }

    }
}
