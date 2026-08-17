package com.seneca_brito.lab_manager.application.Commands.usuario.update;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioUpdateDTO;
import com.seneca_brito.lab_manager.shared.ENUM.TipoDeUsuarios;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.RegistroDuplicadoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUsuarioHandlerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void partiallyUpdatesAllowedFieldsAndPreservesTheOthers() {
        Usuario usuario = usuario();
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Nome Atualizado", null, null, "20260001");
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByMatriculaAndIdNot(dto.matricula(), usuario.getId()))
                .thenReturn(false);
        when(usuarioRepository.saveAndFlush(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        UpdateUsuarioHandler handler = new UpdateUsuarioHandler(usuarioRepository);

        Usuario atualizado = handler.update(usuario.getId(), dto);

        assertSame(usuario, atualizado);
        assertEquals("Nome Atualizado", atualizado.getNome());
        assertEquals("usuario@example.com", atualizado.getEmail());
        assertEquals("Computacao", atualizado.getCurso());
        assertEquals("20260001", atualizado.getMatricula());
        assertEquals(TipoDeUsuarios.PROF, atualizado.getTipoDeUsuarios());
    }

    @Test
    void rejectsMatriculaOwnedByAnotherUsuario() {
        Usuario usuario = usuario();
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO(null, null, null, "OUTRA-MATRICULA");
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByMatriculaAndIdNot(dto.matricula(), usuario.getId()))
                .thenReturn(true);
        UpdateUsuarioHandler handler = new UpdateUsuarioHandler(usuarioRepository);

        assertThrows(RegistroDuplicadoException.class,
                () -> handler.update(usuario.getId(), dto));
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void returnsNotFoundForUnknownUsuario() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());
        UpdateUsuarioHandler handler = new UpdateUsuarioHandler(usuarioRepository);

        assertThrows(RecursoNaoEncontradoException.class,
                () -> handler.update(id, new UsuarioUpdateDTO(null, null, null, null)));
    }

    @Test
    void updatesAuthenticatedUsuarioByEmailInsteadOfClientProvidedId() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.saveAndFlush(usuario)).thenReturn(usuario);
        UpdateUsuarioHandler handler = new UpdateUsuarioHandler(usuarioRepository);

        Usuario atualizado = handler.updateAuthenticated(usuario.getEmail(),
                new UsuarioUpdateDTO(null, null, "Engenharia", null));

        assertEquals("Engenharia", atualizado.getCurso());
        verify(usuarioRepository).findByEmail(usuario.getEmail());
        verify(usuarioRepository, never()).findById(any());
    }

    private static Usuario usuario() {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Usuario Teste")
                .email("usuario@example.com")
                .senha("hash")
                .curso("Computacao")
                .matricula("20260001")
                .tipoDeUsuarios(TipoDeUsuarios.PROF)
                .build();
    }
}
