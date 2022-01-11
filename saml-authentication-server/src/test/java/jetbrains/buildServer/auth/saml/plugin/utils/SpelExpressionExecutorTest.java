package jetbrains.buildServer.auth.saml.plugin.utils;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.settings.Saml2Settings;
import jetbrains.buildServer.auth.saml.plugin.SamlAuthenticationScheme;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import junit.framework.TestCase;
import lombok.SneakyThrows;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpelExpressionExecutorTest {
    private SpelExpressionExecutor executor;
    private SpelExpressionContext context;

    @Before
    public void setUp() throws Exception {
        this.executor = new SpelExpressionExecutor();

        var props = new HashMap<String, String>();
        props.put("var1", "Var1");
        props.put("var2", "Var2");

        this.context = new SpelExpressionContext(props);
    }

    @Test
    public void shouldEvaluateExpressionInStandardContext() {
        var result = executor.evaluate("var1 + ' ' + var2", context);
        assertThat(result, equalTo("Var1 Var2"));
    }

    @Test
    public void shouldSupportAttributesWithSpaces() {
        Map<String, Object> map = this.context.getRootObjectAsMap();
        map.put("Variable with long name", "Value");
        var result = executor.evaluate("get('Variable with long name')", context);
        assertThat(result, equalTo("Value"));
    }

    @Test
    public void shouldSupportListLikeAttributes() {
        var map = this.context.getRootObjectAsMap();

    }

    @SneakyThrows
    @Test
    public void shouldBuildContextForSaml() {
        var request = mock(HttpServletRequest.class);
        var samlResponsePath = "src/test/resources/saml_signed_with_attributes.xml";

        var callbackUrl = "http://sp.example.com/demo1/index.php?acs";
        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        // built using https://capriza.github.io/samling/samling.html#samlPropertiesTab
        var saml = FileUtils.readFileToString(Paths.get(samlResponsePath).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());

        var parameterMap = new HashMap<String, String[]>();
        parameterMap.put("SAMLResponse", new String[] {saml});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("SAMLResponse")).thenReturn(saml);
        var response = mock(HttpServletResponse.class);

        var settings = new SamlPluginSettings();
        settings.setIssuerUrl("http://idp.example.com/metadata.php");
        settings.setSsoEndpoint("http://idp.example.com/metadata.php");
        settings.setEntityId("http://sp.example.com/demo1/metadata.php");
        settings.setPublicCertificate("-----BEGIN CERTIFICATE-----\n" +
                "MIICpzCCAhACCQDuFX0Db5iljDANBgkqhkiG9w0BAQsFADCBlzELMAkGA1UEBhMC\n" +
                "VVMxEzARBgNVBAgMCkNhbGlmb3JuaWExEjAQBgNVBAcMCVBhbG8gQWx0bzEQMA4G\n" +
                "A1UECgwHU2FtbGluZzEPMA0GA1UECwwGU2FsaW5nMRQwEgYDVQQDDAtjYXByaXph\n" +
                "LmNvbTEmMCQGCSqGSIb3DQEJARYXZW5naW5lZXJpbmdAY2Fwcml6YS5jb20wHhcN\n" +
                "MTgwNTE1MTgxMTEwWhcNMjgwNTEyMTgxMTEwWjCBlzELMAkGA1UEBhMCVVMxEzAR\n" +
                "BgNVBAgMCkNhbGlmb3JuaWExEjAQBgNVBAcMCVBhbG8gQWx0bzEQMA4GA1UECgwH\n" +
                "U2FtbGluZzEPMA0GA1UECwwGU2FsaW5nMRQwEgYDVQQDDAtjYXByaXphLmNvbTEm\n" +
                "MCQGCSqGSIb3DQEJARYXZW5naW5lZXJpbmdAY2Fwcml6YS5jb20wgZ8wDQYJKoZI\n" +
                "hvcNAQEBBQADgY0AMIGJAoGBAJEBNDJKH5nXr0hZKcSNIY1l4HeYLPBEKJLXyAno\n" +
                "FTdgGrvi40YyIx9lHh0LbDVWCgxJp21BmKll0CkgmeKidvGlr3FUwtETro44L+Sg\n" +
                "mjiJNbftvFxhNkgA26O2GDQuBoQwgSiagVadWXwJKkodH8tx4ojBPYK1pBO8fHf3\n" +
                "wOnxAgMBAAEwDQYJKoZIhvcNAQELBQADgYEACIylhvh6T758hcZjAQJiV7rMRg+O\n" +
                "mb68iJI4L9f0cyBcJENR+1LQNgUGyFDMm9Wm9o81CuIKBnfpEE2Jfcs76YVWRJy5\n" +
                "xJ11GFKJJ5T0NEB7txbUQPoJOeNoE736lF5vYw6YKp8fJqPW0L2PLWe9qTn8hxpd\n" +
                "njo3k6r5gXyl8tk=\n" +
                "-----END CERTIFICATE-----\n");

        settings.setStrict(false);

        var auth = new Auth(SamlAuthenticationScheme.buildSettings(settings, new URL("http://localhost")), request, response);
        auth.processResponse();

        var context = new SpelExpressionContext(auth);
        var result = executor.evaluate("fullname + ' ' + email", context);
        assertThat(result, equalTo("Full Name myemail.com"));
    }
}