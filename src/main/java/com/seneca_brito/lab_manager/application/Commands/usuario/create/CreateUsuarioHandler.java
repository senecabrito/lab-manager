package com.seneca_brito.lab_manager.application.Commands.usuario.create;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateUsuarioHandler {

    private final UsuarioRepository usuarioRepository;


    public Usuario create(@Valid Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
