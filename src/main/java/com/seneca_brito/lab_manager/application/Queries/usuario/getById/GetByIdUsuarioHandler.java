package com.seneca_brito.lab_manager.application.Queries.usuario.getById;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetByIdUsuarioHandler {

    private final UsuarioRepository usuarioRepository;


    public Optional<Usuario> findById(UUID id) {
        return usuarioRepository.findById(id);
    }


}
