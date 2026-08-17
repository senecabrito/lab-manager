package com.seneca_brito.lab_manager.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(CorsConfigTest.TestConfiguration.class)
@WebAppConfiguration
@TestPropertySource(properties = "lab-manager.cors.allowed-origins=https://frontend.example")
class CorsConfigTest {

    private static final String ALLOWED_ORIGIN = "https://frontend.example";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void allowsConfiguredOriginWithoutCredentials() throws Exception {
        mockMvc.perform(get("/cors-test")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void rejectsUnknownOrigin() throws Exception {
        mockMvc.perform(get("/cors-test")
                        .header(HttpHeaders.ORIGIN, "https://origem-nao-autorizada.invalid"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @Test
    void allowsPreflightWithRequiredMethodAndHeaders() throws Exception {
        mockMvc.perform(options("/cors-test")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                "authorization,content-type,accept"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN))
                .andExpect(result -> {
                    String methods = result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
                    assertEquals(Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
                            splitHeader(methods));

                    String headers = result.getResponse().getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
                    assertNotNull(headers);
                    String normalizedHeaders = headers.toLowerCase(Locale.ROOT);
                    assertTrue(normalizedHeaders.contains("authorization"));
                    assertTrue(normalizedHeaders.contains("content-type"));
                    assertTrue(normalizedHeaders.contains("accept"));
                });
    }

    @Test
    void rejectsMethodOutsidePolicy() throws Exception {
        mockMvc.perform(options("/cors-test")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "HEAD"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsHeaderOutsidePolicy() throws Exception {
        mockMvc.perform(options("/cors-test")
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "x-custom-header"))
                .andExpect(status().isForbidden());
    }

    private static Set<String> splitHeader(String value) {
        assertNotNull(value);
        Set<String> values = Arrays.stream(value.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        assertFalse(values.isEmpty());
        return values;
    }

    @Configuration
    @EnableWebMvc
    @Import({CorsConfig.class, TestController.class})
    static class TestConfiguration {
    }

    @RestController
    static class TestController {

        @GetMapping("/cors-test")
        String get() {
            return "ok";
        }
    }
}
