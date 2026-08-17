package com.seneca_brito.lab_manager.application.Queries.usuario.getProfile;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioResponseDTO;
import com.seneca_brito.lab_manager.shared.mappers.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class GetPerfilUsuario {

    private final GetPerfilUsuarioHandler usuarioHandler;
    private final UsuarioMapper usuarioMapper;

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getProfile(Authentication authentication) {
        Usuario usuario = usuarioHandler.findAuthenticated(authentication.getName());
        return ResponseEntity.ok(usuarioMapper.toDto(usuario));
    }
}
