package com.seneca_brito.lab_manager.application.Queries.usuario.getProfile;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPerfilUsuarioHandler {

    private final UsuarioRepository usuarioRepository;

    public Usuario findAuthenticated(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario autenticado nao encontrado"));
    }
}
