package com.seneca_brito.lab_manager.application.Commands.autenticacao.cadastro;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.RolesRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.infrastructure.security.RolesEntity;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioRequestDTO;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.ENUM.TipoDeUsuarios;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastroHandlerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RolesRepository rolesRepository;

    @Test
    void persistsAllMandatoryUsuarioFields() {
        RolesEntity role = RolesEntity.builder()
                .id(UUID.randomUUID())
                .nome(RoleTypeEnum.USUARIO.name())
                .build();
        UsuarioRequestDTO dto = new UsuarioRequestDTO(
                "Usuario Teste", "usuario@example.com", "Senha#123", "Computacao", "20260001");
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
        when(usuarioRepository.existsByMatricula(dto.matricula())).thenReturn(false);
        when(rolesRepository.findByNome(RoleTypeEnum.USUARIO.name())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(dto.senha())).thenReturn("encoded-password");
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CadastroHandler handler = new CadastroHandler(
                usuarioRepository, passwordEncoder, rolesRepository);

        Usuario usuario = handler.create(dto);

        assertEquals(dto.nome(), usuario.getNome());
        assertEquals(dto.email(), usuario.getEmail());
        assertEquals(dto.curso(), usuario.getCurso());
        assertEquals(dto.matricula(), usuario.getMatricula());
        assertEquals("encoded-password", usuario.getSenha());
        assertNotEquals(dto.senha(), usuario.getSenha());
        assertEquals(TipoDeUsuarios.PROF, usuario.getTipoDeUsuarios());
        assertTrue(usuario.getRoles().contains(role));
    }

    @Test
    void rejectsDuplicateMatriculaBeforePersistence() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO(
                "Usuario Teste", "usuario@example.com", "Senha#123", "Computacao", "20260001");
        when(usuarioRepository.existsByEmail(dto.email())).thenReturn(false);
        when(usuarioRepository.existsByMatricula(dto.matricula())).thenReturn(true);
        CadastroHandler handler = new CadastroHandler(
                usuarioRepository, passwordEncoder, rolesRepository);

        assertThrows(com.seneca_brito.lab_manager.shared.exceptions.RegistroDuplicadoException.class,
                () -> handler.create(dto));
        verify(usuarioRepository, never()).saveAndFlush(any());
    }
}
