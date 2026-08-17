package com.seneca_brito.lab_manager.application.Commands.usuario.update;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioResponseDTO;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioUpdateDTO;
import com.seneca_brito.lab_manager.shared.mappers.UsuarioMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UpdateUsuarioCommand {

    private final UpdateUsuarioHandler usuarioHandler;
    private final UsuarioMapper usuarioMapper;

    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> update(@PathVariable UUID id,
                                                      @RequestBody @Valid UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioHandler.update(id, dto);
        return ResponseEntity.ok(usuarioMapper.toDto(usuario));
    }

    @PatchMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> updateProfile(Authentication authentication,
                                                             @RequestBody @Valid UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioHandler.updateAuthenticated(authentication.getName(), dto);
        return ResponseEntity.ok(usuarioMapper.toDto(usuario));
    }
}
