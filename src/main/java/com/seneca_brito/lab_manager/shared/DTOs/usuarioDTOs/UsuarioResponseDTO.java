package com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.TipoDeUsuarios;

import java.util.UUID;

public record UsuarioResponseDTO(
        UUID id,
        String nome,
        String email,
        TipoDeUsuarios tipoDeUsuarios,
        String curso
) {
}
