package org.gromozeka.teamcity.saml.runner;

import lombok.extern.java.Log;
import lombok.var;
import org.gromozeka.teamcity.saml.core.SamlAuthenticationProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.net.URL;
import java.util.Locale;

@Log
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        log.info("Starting SAML teamcity plugin runner");
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public ITemplateResolver teamcityPluginTemplateResolver() {
        URL pluginResources = SamlAuthenticationProvider.class.getResource("/buildServerResources/");

        var resolver = new FileTemplateResolver();
        resolver.setPrefix(pluginResources.getPath());
        resolver.setSuffix(".jsp");

        return resolver;
    }
}
