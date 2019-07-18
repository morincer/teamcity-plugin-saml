package jetbrains.buildServer.controllers.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonActionError {
    int code;
    String messages;
}
