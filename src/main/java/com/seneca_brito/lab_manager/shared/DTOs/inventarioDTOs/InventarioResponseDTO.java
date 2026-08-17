package com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs;

import java.util.UUID;

public record InventarioResponseDTO(
        UUID id,
        String nome,
        Integer quantidadeDisponivel,
        Integer quantidadeIndisponivel,
        UUID laboratorioId,
        String laboratorio
) {
}
