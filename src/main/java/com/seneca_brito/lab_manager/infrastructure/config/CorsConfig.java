package com.seneca_brito.lab_manager.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String[] ALLOWED_METHODS = {
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    };

    private static final String[] ALLOWED_HEADERS = {
            "Authorization", "Content-Type", "Accept"
    };

    private final String[] allowedOrigins;

    public CorsConfig(@Value("${lab-manager.cors.allowed-origins:}") String configuredOrigins) {
        this.allowedOrigins = parseAllowedOrigins(configuredOrigins);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders(ALLOWED_HEADERS)
                .allowCredentials(false);
    }

    private static String[] parseAllowedOrigins(String configuredOrigins) {
        String[] origins = Arrays.stream(configuredOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .distinct()
                .toArray(String[]::new);

        if (Arrays.asList(origins).contains("*")) {
            throw new IllegalArgumentException("CORS does not allow wildcard origins");
        }

        return origins;
    }
}
