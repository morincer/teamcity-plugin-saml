package jetbrains.buildServer.auth.saml.plugin;

import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import lombok.var;
import org.apache.log4j.BasicConfigurator;
import org.apache.xerces.impl.dv.util.Base64;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.testng.reporters.Files;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SamlAuthenticationSchemeTest {

    SamlAuthenticationScheme scheme;
    private InMemorySamlPluginSettingsStorage settingsStorage;
    private UserModel userModel;
    private SUser validUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BasicConfigurator.configure();
    }

    @Before
    public void setUp() throws Exception {
        this.settingsStorage = new InMemorySamlPluginSettingsStorage();

        this.validUser = mock(SUser.class);
        when(validUser.getUsername()).thenReturn("valid_user");
        when(validUser.getName()).thenReturn("Valid User");

        this.userModel = mock(UserModel.class);
        when(userModel.findUserAccount(null, "valid_user")).thenReturn(validUser);

        this.scheme = new SamlAuthenticationScheme(settingsStorage, userModel);
    }

    @Test
    public void shouldAuthenticateValidSamlClaimForValidUser() throws IOException {
        var request = mock(HttpServletRequest.class);
        var samlResponsePath = "src/test/resources/saml_signed_message.xml";

        var callbackUrl = "http://sp.example.com/demo1/index.php?acs";

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

        this.settingsStorage.save(settings);

        // built using https://capriza.github.io/samling/samling.html#samlPropertiesTab
        var saml = Files.readFile(Paths.get(samlResponsePath).toAbsolutePath().toFile());
        saml = Base64.encode(saml.getBytes());

        var parameterMap = new HashMap<String, String[]>();
        parameterMap.put("SAMLResponse", new String[] {saml});

        when(request.getParameterMap()).thenReturn(parameterMap);
        when(request.getParameter("SAMLResponse")).thenReturn(saml);

        when(request.getRequestURL()).thenReturn(new StringBuffer(callbackUrl));

        var response = mock(HttpServletResponse.class);

        var result = this.scheme.processAuthenticationRequest(request, response, new HashMap<>());
        assertThat(result.getType(), equalTo(HttpAuthenticationResult.Type.AUTHENTICATED));
    }
}
