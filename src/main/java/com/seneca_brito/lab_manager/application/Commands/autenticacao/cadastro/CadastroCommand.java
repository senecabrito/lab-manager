package com.seneca_brito.lab_manager.application.Commands.autenticacao.cadastro;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioRequestDTO;
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
@RequestMapping("/autenticacao")
@RequiredArgsConstructor
public class CadastroCommand {

    private final CadastroHandler cadastroHandler;
    private final UsuarioMapper usuarioMapper;

    @PostMapping("/cadastro")
    public ResponseEntity<Void> create(@RequestBody @Valid UsuarioRequestDTO dto) {
        Usuario response = cadastroHandler.create(dto);
        String id = response.getId().toString();

        return ResponseEntity.created(URI.create(id)).build();
    }
}
