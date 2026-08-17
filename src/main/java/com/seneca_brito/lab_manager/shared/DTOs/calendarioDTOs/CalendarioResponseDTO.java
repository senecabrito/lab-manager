package com.seneca_brito.lab_manager.shared.DTOs.calendarioDTOs;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CalendarioResponseDTO(
        UUID laboratorioId,
        LocalDate data,
        @Schema(example = "America/Sao_Paulo", allowableValues = "America/Sao_Paulo") String timezone,
        @ArraySchema(minItems = 21, maxItems = 21,
                arraySchema = @Schema(description = "Os 21 intervalos de 30 minutos entre 07:30 e 18:00"))
        List<CalendarioSlotDTO> slots
) {
}
