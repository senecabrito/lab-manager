package com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record RecomendacaoOpcaoDTO(
        UUID laboratorioId,
        String laboratorio,
        Integer capacidade,
        String localizacao,
        Set<String> recursos,
        LocalDate data,
        @Schema(type = "string", format = "time", example = "10:00:00") LocalTime inicio,
        @Schema(type = "string", format = "time", example = "11:00:00") LocalTime fim,
        boolean disponivel
) {
}
