package jetbrains.buildServer.controllers.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletResponse;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonActionError {
    int code;
    String message;

    public JsonActionError(String message) {
        this(HttpServletResponse.SC_BAD_REQUEST, message);
    }
}
