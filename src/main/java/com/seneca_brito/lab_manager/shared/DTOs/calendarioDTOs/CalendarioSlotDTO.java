package com.seneca_brito.lab_manager.shared.DTOs.calendarioDTOs;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

public record CalendarioSlotDTO(
        @Schema(type = "string", format = "time", example = "07:30:00") LocalTime inicio,
        @Schema(type = "string", format = "time", example = "08:00:00") LocalTime fim,
        boolean ocupado
) {
}
