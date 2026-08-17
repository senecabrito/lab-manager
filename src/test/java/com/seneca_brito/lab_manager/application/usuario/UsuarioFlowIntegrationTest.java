package com.seneca_brito.lab_manager.application.usuario;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.RolesRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.infrastructure.security.RolesEntity;
import com.seneca_brito.lab_manager.infrastructure.security.config.TokenProvider;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.ENUM.TipoDeUsuarios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioFlowIntegrationTest {

    private static final UUID ADMIN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USUARIO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private RolesRepository rolesRepository;

    @MockitoBean
    private TokenProvider tokenProvider;

    private Usuario admin;
    private Usuario usuario;
    private RolesEntity usuarioRole;

    @BeforeEach
    void setUp() {
        RolesEntity adminRole = RolesEntity.builder()
                .id(UUID.randomUUID())
                .nome(RoleTypeEnum.ADMINISTRACAO.name())
                .build();
        usuarioRole = RolesEntity.builder()
                .id(UUID.randomUUID())
                .nome(RoleTypeEnum.USUARIO.name())
                .build();
        admin = usuario(ADMIN_ID, "Administrador", "admin@example.com", "ADM-001",
                TipoDeUsuarios.ADMIN, Set.of(adminRole));
        usuario = usuario(USUARIO_ID, "Usuario Teste", "usuario@example.com", "20260001",
                TipoDeUsuarios.PROF, Set.of(usuarioRole));

        when(tokenProvider.isTokenValid("admin-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("admin-token")).thenReturn(admin.getEmail());
        when(tokenProvider.isTokenValid("usuario-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("usuario-token")).thenReturn(usuario.getEmail());
        when(usuarioRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
    }

    @Test
    void validRegistrationPersistsEncodedPasswordAndMatricula() throws Exception {
        prepareRegistrationPersistence();

        mockMvc.perform(post("/api/v1/autenticacao/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistrationJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION,
                        "/api/v1/usuarios/33333333-3333-3333-3333-333333333333"));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(captor.capture());
        Usuario persisted = captor.getValue();
        assertEquals("20260002", persisted.getMatricula());
        assertNotEquals("Senha#123", persisted.getSenha());
        assertTrue(passwordEncoder.matches("Senha#123", persisted.getSenha()));
    }

    @Test
    void duplicateMatriculaReturnsStandardConflict() throws Exception {
        when(usuarioRepository.existsByEmail("novo@example.com")).thenReturn(false);
        when(usuarioRepository.existsByMatricula("20260002")).thenReturn(true);

        mockMvc.perform(post("/api/v1/autenticacao/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistrationJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Matricula ja esta em uso"));

        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void databaseUniquenessRaceReturnsControlledConflict() throws Exception {
        prepareRegistrationPersistence();
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("uk_usuario_matricula"));

        mockMvc.perform(post("/api/v1/autenticacao/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegistrationJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Operacao conflitante com a integridade dos dados"));
    }

    @Test
    void invalidRequiredRegistrationFieldsReturnValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/autenticacao/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\",\"senha\":\"123\","
                                + "\"curso\":\"\",\"matricula\":\"\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.erro").isArray());
    }

    @Test
    void listingUsesMinimalDtoAndPagedEnvelope() throws Exception {
        when(usuarioRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(usuario)));

        mockMvc.perform(get("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value(usuario.getNome()))
                .andExpect(jsonPath("$.content[0].curso").value(usuario.getCurso()))
                .andExpect(jsonPath("$.content[0].id").doesNotExist())
                .andExpect(jsonPath("$.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.content[0].matricula").doesNotExist())
                .andExpect(jsonPath("$.content[0].senha").doesNotExist());
    }

    @Test
    void emptyListingReturnsEmptyPagedModelInsteadOfNotFound() throws Exception {
        when(usuarioRepository.findAll(any(Pageable.class)))
                .thenAnswer(invocation -> Page.empty(invocation.getArgument(0)));

        mockMvc.perform(get("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    void detailReturnsAllowedFieldsWithoutPassword() throws Exception {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/api/v1/usuarios/{id}", USUARIO_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USUARIO_ID.toString()))
                .andExpect(jsonPath("$.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.matricula").value(usuario.getMatricula()))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles").doesNotExist());
    }

    @Test
    void unknownDetailReturnsStandardNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/usuarios/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Usuario nao encontrado"));
    }

    @Test
    void invalidUserIdReturnsStandardBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/invalid-uuid")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Requisicao invalida"));
    }

    @Test
    void authenticatedProfileComesFromJwtIdentityAndHidesPassword() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer usuario-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USUARIO_ID.toString()))
                .andExpect(jsonPath("$.email").value(usuario.getEmail()))
                .andExpect(jsonPath("$.matricula").value(usuario.getMatricula()))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(usuarioRepository, org.mockito.Mockito.atLeast(2)).findByEmail(usuario.getEmail());
    }

    @Test
    void profileWithoutAuthenticationReturnsStandardUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Autenticacao obrigatoria"));
    }

    @Test
    void regularUsuarioCannotAccessAdministrativeListing() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer usuario-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void authenticatedUsuarioCanPartiallyUpdateOwnProfileWithoutPrivilegeChange() throws Exception {
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/api/v1/usuarios/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer usuario-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nome Atualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
                .andExpect(jsonPath("$.email").value("usuario@example.com"))
                .andExpect(jsonPath("$.matricula").value("20260001"))
                .andExpect(jsonPath("$.tipoDeUsuarios").value("PROF"));
    }

    @Test
    void administratorCanDeleteExistingUsuario() throws Exception {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

        mockMvc.perform(delete("/api/v1/usuarios/{id}", USUARIO_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(header().doesNotExist(HttpHeaders.CONTENT_TYPE));

        verify(usuarioRepository).delete(usuario);
        verify(usuarioRepository).flush();
    }

    @Test
    void deletionIntegrityConflictIsControlledAndDoesNotCascade() throws Exception {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        doThrow(new DataIntegrityViolationException("fk_reserva_usuario"))
                .when(usuarioRepository).flush();

        mockMvc.perform(delete("/api/v1/usuarios/{id}", USUARIO_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer admin-token"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(usuarioRepository).delete(usuario);
    }

    private void prepareRegistrationPersistence() {
        when(usuarioRepository.existsByEmail("novo@example.com")).thenReturn(false);
        when(usuarioRepository.existsByMatricula("20260002")).thenReturn(false);
        when(rolesRepository.findByNome(RoleTypeEnum.USUARIO.name()))
                .thenReturn(Optional.of(usuarioRole));
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario saved = invocation.getArgument(0);
            saved.setId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
            return saved;
        });
    }

    private static String validRegistrationJson() {
        return "{\"nome\":\"Novo Usuario\",\"email\":\"novo@example.com\","
                + "\"senha\":\"Senha#123\",\"curso\":\"Computacao\","
                + "\"matricula\":\"20260002\"}";
    }

    private static Usuario usuario(UUID id, String nome, String email, String matricula,
                                    TipoDeUsuarios tipo, Set<RolesEntity> roles) {
        return Usuario.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .senha("hash-nao-exposto")
                .curso("Computacao")
                .matricula(matricula)
                .tipoDeUsuarios(tipo)
                .roles(roles)
                .build();
    }
}
