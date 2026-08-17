package com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RecomendacaoReservaResponseDTO(
        @Schema(example = "America/Sao_Paulo", allowableValues = "America/Sao_Paulo") String timezone,
        List<RecomendacaoOpcaoDTO> recomendacoes
) {
}
