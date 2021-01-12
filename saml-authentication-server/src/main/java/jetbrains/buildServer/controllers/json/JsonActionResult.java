package jetbrains.buildServer.controllers.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import jetbrains.buildServer.log.Loggers;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.var;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonActionResult<T> {
    JsonActionError[] errors = null;
    T result = null;

    public static <T> JsonActionResult<T> ok(T result) {
        var jsonActionResult = new JsonActionResult<T>();
        jsonActionResult.result = result;
        return jsonActionResult;
    }


    public static <T> JsonActionResult<T> fail(Throwable e) {
        var jsonActionResult = new JsonActionResult<T>();
        Loggers.SERVER.error(e);
        jsonActionResult.errors = new JsonActionError[] { new JsonActionError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage()) };
        return jsonActionResult;
    }

    public static <T> JsonActionResult<T> fail(List<JsonActionError> errors) {
        var jsonActionResult = new JsonActionResult<T>();
        jsonActionResult.errors = errors.toArray(new JsonActionError[0]);
        return jsonActionResult;
    }

    public static <T> JsonActionResult<T> fail(JsonActionError ... errors) {
        var jsonActionResult = new JsonActionResult<T>();
        jsonActionResult.errors = errors;
        return jsonActionResult;
    }

    public static <T> JsonActionResult<T> fail(Set<ConstraintViolation<T>> errors) {
        String message = errors.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
        return fail(new JsonActionError(HttpServletResponse.SC_OK, message));
    }

    public static <T> JsonActionResult<T> forbidden() {
        String message = "You are not allowed to perform this action. Please contact your system administrator";
        return fail(new JsonActionError(HttpServletResponse.SC_OK, message));
    }
}
