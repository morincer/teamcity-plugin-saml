package jetbrains.buildServer.controllers.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import jetbrains.buildServer.auth.saml.plugin.SamlPluginSettings;
import jetbrains.buildServer.log.Loggers;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.var;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonActionResult<T> {
    JsonActionError[] errors = null;
    T result = null;

    public static JsonActionResult ok(Object result) {
        var jsonActionResult = new JsonActionResult();
        jsonActionResult.result = result;
        return jsonActionResult;
    }

    public static JsonActionResult fail(Throwable e) {
        var jsonActionResult = new JsonActionResult();
        Loggers.SERVER.error(e);
        jsonActionResult.errors = new JsonActionError[] { new JsonActionError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage()) };
        return jsonActionResult;
    }

    public static JsonActionResult fail(JsonActionError ... errors) {
        var jsonActionResult = new JsonActionResult();
        jsonActionResult.errors = errors;
        return jsonActionResult;
    }

    public static <T> JsonActionResult fail(Set<ConstraintViolation<T>> errors) {
        String message = errors.stream().map(e -> e.getMessage()).collect(Collectors.joining(", "));
        return fail(new JsonActionError(HttpServletResponse.SC_OK, message));
    }
}
