package com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.CategoriaProblema;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReclamacaoResponseDTO(
        UUID id,
        String descricao,
        CategoriaProblema categoriaProblema,
        @Schema(format = "date-time", description = "Data e hora local em America/Sao_Paulo")
        LocalDateTime dataReclamacao,
        StatusReclamacao status,
        UUID usuarioId,
        UUID laboratorioId
) {
}
