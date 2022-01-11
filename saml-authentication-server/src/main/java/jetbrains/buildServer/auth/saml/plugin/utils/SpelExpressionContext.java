package jetbrains.buildServer.auth.saml.plugin.utils;

import com.onelogin.saml2.Auth;
import lombok.var;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpelExpressionContext extends StandardEvaluationContext {

    public SpelExpressionContext(Map<String, ?> attributes) {
        setRootObject(attributes);
        addPropertyAccessor(new MapAccessor());
    }

    public SpelExpressionContext(Auth auth) {
        Map<String, Object> attributes = new HashMap<>();

        for (var entry : auth.getAttributes().entrySet()) {
            var name = entry.getKey();
            var value = entry.getValue() == null || entry.getValue().size() == 0
                    ? ""
                    : entry.getValue().size() == 1 ? entry.getValue().get(0) : entry.getValue();

            attributes.put(name, value);
        }

        attributes.put("nameid", auth.getNameId());
        attributes.put("lastassertionid", auth.getLastAssertionId());

        setRootObject(attributes);
        addPropertyAccessor(new MapAccessor());
    }

    public <T> Map<String, T> getRootObjectAsMap() {
        return (Map<String, T>) getRootObject().getValue();
    }
}
