package org.gromozeka.teamcity.saml.runner;

import org.gromozeka.teamcity.saml.core.SamlAuthenticationProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.net.URL;

@Controller
public class PluginController {

    @RequestMapping("/config")
    public String getTest() {
        return "SamlConfigure";
    }
}
