package com.seneca_brito.lab_manager.shared.DTOs.acessoDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusAcesso;

import java.time.Instant;
import java.util.UUID;

public record AcessoResponseDTO(
        UUID id,
        UUID reservaId,
        UUID usuarioId,
        UUID laboratorioId,
        String laboratorio,
        Instant checkIn,
        Instant checkOut,
        StatusAcesso status
) {
}
