package com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.CategoriaProblema;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReclamacaoResponseDTO(
        UUID id,
        String descricao,
        CategoriaProblema categoriaProblema,
        LocalDateTime dataReclamacao,
        StatusReclamacao status,
        UUID usuarioId,
        UUID laboratorioId
) {
}
