package jetbrains.buildServer.controllers.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import jetbrains.buildServer.auth.saml.plugin.SamlPluginSettings;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.var;

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

    public static JsonActionResult fail(JsonActionError ... errors) {
        var jsonActionResult = new JsonActionResult();
        jsonActionResult.errors = errors;
        return jsonActionResult;
    }
}
