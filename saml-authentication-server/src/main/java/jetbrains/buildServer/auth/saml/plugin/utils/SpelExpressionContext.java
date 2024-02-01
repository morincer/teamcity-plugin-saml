package jetbrains.buildServer.auth.saml.plugin.utils;

import com.onelogin.saml2.Auth;
import lombok.var;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import java.util.HashMap;
import java.util.Map;

public class SpelExpressionContext {
    private final EvaluationContext evaluationContext;

    public SpelExpressionContext(Map<String, ?> attributes) {
        evaluationContext = new SimpleEvaluationContext.Builder(new MapAccessor()).withInstanceMethods().withRootObject(attributes).build();
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

        evaluationContext = new SimpleEvaluationContext.Builder(new MapAccessor()).withRootObject(attributes).build();
    }

    public <T> Map<String, T> getRootObjectAsMap() {
        return (Map<String, T>) evaluationContext.getRootObject().getValue();
    }

    public EvaluationContext getEvaluationContext() {
        return evaluationContext;
    }
}
