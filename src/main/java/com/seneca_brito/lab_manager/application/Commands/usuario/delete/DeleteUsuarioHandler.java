package com.seneca_brito.lab_manager.application.Commands.usuario.delete;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteUsuarioHandler {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void delete(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));
        usuarioRepository.delete(usuario);
        usuarioRepository.flush();
    }
}
