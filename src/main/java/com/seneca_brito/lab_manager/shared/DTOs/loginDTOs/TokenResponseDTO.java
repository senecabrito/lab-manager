package com.seneca_brito.lab_manager.shared.DTOs.loginDTOs;

public record TokenResponseDTO(
        String token,
        long expiresIn
) {
}
