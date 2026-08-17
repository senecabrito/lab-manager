package com.seneca_brito.lab_manager.application.Commands.autenticacao.cadastro;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/autenticacao")
@RequiredArgsConstructor
public class CadastroCommand {

    private final CadastroHandler cadastroHandler;

    @PostMapping("/cadastro")
    public ResponseEntity<Void> create(@RequestBody @Valid UsuarioRequestDTO dto) {
        Usuario response = cadastroHandler.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + response.getId())).build();
    }
}
