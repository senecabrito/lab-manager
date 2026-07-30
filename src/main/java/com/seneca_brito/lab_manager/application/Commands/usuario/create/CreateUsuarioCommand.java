package com.seneca_brito.lab_manager.application.Commands.usuario.create;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.UsuarioRequestDTO;
import com.seneca_brito.lab_manager.shared.mappers.UsuarioMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class CreateUsuarioCommand{

    private final CreateUsuarioHandler usuarioHandler;
    private final UsuarioMapper usuarioMapper;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid UsuarioRequestDTO UsuarioDTO) {
        Usuario usuario = usuarioMapper.toModel(UsuarioDTO);

        Usuario response = usuarioHandler.create(usuario);
        String id = response.getId().toString();

        return ResponseEntity.created(URI.create(id)).build();
    }
}
