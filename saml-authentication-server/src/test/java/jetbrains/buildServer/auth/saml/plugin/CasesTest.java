package jetbrains.buildServer.auth.saml.plugin;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.authn.SamlResponse;
import com.onelogin.saml2.exception.SettingsException;
import com.onelogin.saml2.http.HttpRequest;
import com.onelogin.saml2.settings.Saml2Settings;
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.auth.saml.plugin.pojo.MetadataImport;
import jetbrains.buildServer.auth.saml.plugin.pojo.SamlPluginSettings;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.users.UserModel;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTimeUtils;
import org.joda.time.Instant;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CasesTest {
    @Test
    public void case1ShouldPass() throws Exception {
        SamlPluginSettingsStorage storage = new SamlPluginSettingsStorageImpl(Paths.get("src/test/resources/case 1/saml-plugin.config.json"));
        RootUrlHolder rootUrlHolder = mock(RootUrlHolder.class);

        when(rootUrlHolder.getRootUrl()).thenReturn("https://1057teamcity.sapphirepri.com");

        SamlAuthenticationScheme scheme = new SamlAuthenticationScheme(
                rootUrlHolder,
                storage,
                mock(UserModel.class),
                mock(UserGroupManager.class),
                mock(LoginConfiguration.class));


        SamlPluginSettings settings = storage.load();
        scheme.importMetadataIntoSettings(FileUtils.readFileToString(Paths.get("src/test/resources/case 1/federationmetadata.xml").toFile()),
                settings);

        storage.save(settings);

        Saml2Settings saml2Settings = scheme.buildSettings();

        var request = new HttpRequest("https://1057teamcity.sapphirepri.com/app/saml/callback/", "");
        request = request.addParameter(SamlPluginConstants.SAML_RESPONSE_REQUEST_PARAMETER, URLDecoder.decode(
                FileUtils.readFileToString(Paths.get("src/test/resources/case 1/SAMLResponse").toFile()), "utf-8"));

        SamlResponse samlResponse = new SamlResponse(saml2Settings, request);
        DateTimeUtils.setCurrentMillisFixed(Instant.parse("2020-07-25T01:19:25.443Z").getMillis());
        assertTrue(samlResponse.isValid());

        /*var auth = new Auth(saml2Settings, request, mock(HttpServletResponse.class));

        auth.processResponse();*/
    }
}
