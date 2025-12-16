package com.grabbill.server.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.OPTIONS;

/**
 * @author michaellow
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Value("${security.cors.allowed-origin}")
    private String[] corsAllowedOrigin;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(corsAllowedOrigin)
                .allowedMethods(GET.name(), POST.name(), PUT.name(), PATCH.name(), DELETE.name(), OPTIONS.name())
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

}
