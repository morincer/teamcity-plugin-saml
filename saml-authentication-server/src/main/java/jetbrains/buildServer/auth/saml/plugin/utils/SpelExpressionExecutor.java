package jetbrains.buildServer.auth.saml.plugin.utils;

import com.onelogin.saml2.Auth;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SpelExpressionExecutor {

    private SpelExpressionParser parser = new SpelExpressionParser();

    public String validate(String expressionString) {
        try {
            parser.parseExpression(expressionString);
            return null;
        } catch (ParseException e) {
            return e.getMessage();
        }
    }

    public String evaluate(String expressionString, SpelExpressionContext context) {
        log.debug(String.format("Evaluating expression: %s", expressionString));
        var expression = parser.parseExpression(expressionString);
        String result = (String) expression.getValue(context);
        log.debug(String.format("Evaluation result: %s", result));
        return result;
    }
}
