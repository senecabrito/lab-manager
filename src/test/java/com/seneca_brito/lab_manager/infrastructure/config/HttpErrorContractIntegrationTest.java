package com.seneca_brito.lab_manager.infrastructure.config;

import com.seneca_brito.lab_manager.application.Queries.dashboard.DashboardHandler;
import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.infrastructure.security.RolesEntity;
import com.seneca_brito.lab_manager.infrastructure.security.config.TokenProvider;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HttpErrorContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardHandler dashboardHandler;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        RolesEntity adminRole = RolesEntity.builder()
                .nome(RoleTypeEnum.ADMINISTRACAO.name())
                .build();
        Usuario admin = Usuario.builder()
                .email("admin@contract.test")
                .senha("hash-nao-exposto")
                .roles(Set.of(adminRole))
                .build();

        when(tokenProvider.isTokenValid("admin-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("admin-token")).thenReturn(admin.getEmail());
        when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
    }

    @Test
    void unexpectedFailureReturnsSanitizedErroResponse() throws Exception {
        when(dashboardHandler.summary()).thenThrow(new RuntimeException("segredo interno"));

        mockMvc.perform(get("/api/v1/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("segredo"))))
                .andExpect(jsonPath("$.erro").isArray());
    }
}
