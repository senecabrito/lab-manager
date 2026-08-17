package com.seneca_brito.lab_manager.application.Commands.usuario.delete;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUsuarioHandlerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void deletesExistingUsuarioAndFlushesIntegrityChecks() {
        Usuario usuario = Usuario.builder().id(UUID.randomUUID()).build();
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        DeleteUsuarioHandler handler = new DeleteUsuarioHandler(usuarioRepository);

        handler.delete(usuario.getId());

        InOrder order = inOrder(usuarioRepository);
        order.verify(usuarioRepository).delete(usuario);
        order.verify(usuarioRepository).flush();
    }

    @Test
    void returnsNotFoundWithoutDeletingUnknownUsuario() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());
        DeleteUsuarioHandler handler = new DeleteUsuarioHandler(usuarioRepository);

        assertThrows(RecursoNaoEncontradoException.class, () -> handler.delete(id));
        verify(usuarioRepository, never()).delete(org.mockito.ArgumentMatchers.any());
        verify(usuarioRepository, never()).flush();
    }
}
