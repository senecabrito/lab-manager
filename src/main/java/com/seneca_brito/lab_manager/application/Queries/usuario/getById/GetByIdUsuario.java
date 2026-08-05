package com.seneca_brito.lab_manager.application.Queries.usuario.getById;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioMinDTO;
import com.seneca_brito.lab_manager.shared.mappers.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class GetByIdUsuario {

    private final GetByIdUsuarioHandler usuarioHandler;
    private final UsuarioMapper usuarioMapper;

    @GetMapping("/id/{id}")
    public ResponseEntity<UsuarioMinDTO> getById(@PathVariable UUID id){
        Optional<Usuario> usuarioOpt = usuarioHandler.findById(id);

        if (usuarioOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();

        UsuarioMinDTO usuarioMinDto = usuarioMapper.toMinDto(usuario);

        return ResponseEntity.ok(usuarioMinDto);
    }
}
