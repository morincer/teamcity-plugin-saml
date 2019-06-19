package org.gromozeka.teamcity.saml.plugin;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.controllers.FormUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.commons.validator.UrlValidator;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SamlPluginAdminPageController extends BaseFormXmlController {

    private SamlPluginSettingsStorage settingsStorage;

    public SamlPluginAdminPageController(@NotNull SamlPluginSettingsStorage settingsStorage,
                                         @NotNull WebControllerManager webControllerManager) {
        this.settingsStorage = settingsStorage;
        webControllerManager.registerController(SamlPluginConstants.SETTINGS_CONTROLLER_PATH, this);
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse) {
        return null;
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest httpServletRequest, @NotNull HttpServletResponse httpServletResponse, @NotNull Element xmlResponse) {
        SamlPluginSettings settings = new SamlPluginSettings();
        FormUtil.bindFromRequest(httpServletRequest, settings);

        ActionErrors errors = validate(settings);

        if (errors.hasNoErrors()) {
            try {
                settingsStorage.save(settings);
            } catch (IOException e) {
                Loggers.SERVER.error("Failed to store settings in the file: " + e.getMessage(), e);
                errors.addException("ssoUrl", e);
            }

            writeRedirect(xmlResponse,httpServletRequest.getContextPath() + "/admin/admin.html?item=" + SamlPluginConstants.PLUGIN_NAME);
        }

        writeErrors(xmlResponse, errors);

    }

    private ActionErrors validate(@NotNull SamlPluginSettings settings) {
        ActionErrors errors = new ActionErrors();

        UrlValidator urlValidator = new UrlValidator(new String[] {"http", "https"});

        if (StringUtil.isEmptyOrSpaces(settings.getSsoEndpoint())) {
            errors.addError("ssoEndpoint", "IdP SSO URL must not be empty");
        }

        if (!urlValidator.isValid(settings.getSsoEndpoint())) {
            errors.addError("ssoEndpoint", "IdP SSO URL must be a valid URL");
        }

        if (StringUtil.isEmptyOrSpaces(settings.getIssuerUrl())) {
            errors.addError("issuerUrl", "IdP Issuer must not be empty");
        }


        if (StringUtil.isEmptyOrSpaces(settings.getEntityId())) {
            errors.addError("entityId", "IdP Entity Id must not be empty");
        }

        return errors;
    }
}
