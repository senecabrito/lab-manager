package com.seneca_brito.lab_manager.application.Queries.usuario.get;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUsuarioHandler {

    private final UsuarioRepository usuarioRepository;


    public Page<Usuario> findAll(Pageable pageRequest) {
        return usuarioRepository.findAll(pageRequest);
    }
}
