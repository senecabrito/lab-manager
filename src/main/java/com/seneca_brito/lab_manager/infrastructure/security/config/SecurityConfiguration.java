package com.seneca_brito.lab_manager.infrastructure.security.config;

import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.exceptions.errosDTOs.ErroResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authenticationException) ->
                                writeError(response, HttpStatus.UNAUTHORIZED, "Autenticacao obrigatoria"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, HttpStatus.FORBIDDEN, "Acesso negado")))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/autenticacao/cadastro",
                                "/api/v1/autenticacao/login").permitAll()
                        .requestMatchers("/api/v1/usuarios/me").authenticated()
                        .requestMatchers("/api/v1/usuarios", "/api/v1/usuarios/**")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/laboratorios")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/laboratorios/**")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/laboratorios/**")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/laboratorios", "/api/v1/laboratorios/**")
                                .authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/inventario")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/inventario/**")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/inventario/**")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventario", "/api/v1/inventario/**")
                                .authenticated()
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/reservas/*/aprovacao",
                                "/api/v1/reservas/*/rejeicao")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservas")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers("/api/v1/reservas", "/api/v1/reservas/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/acessos")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/acessos/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/reclamacoes/*/status")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/reclamacoes")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .requestMatchers("/api/v1/reclamacoes", "/api/v1/reclamacoes/**").authenticated()
                        .requestMatchers("/api/v1/dashboard", "/api/v1/relatorios/**")
                                .hasAuthority(RoleTypeEnum.ADMINISTRACAO.name())
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration){
        return authenticationConfiguration.getAuthenticationManager();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), ErroResponse.of(status, message));
    }
}
