package com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaUpdateDTO(
        LocalDate dataReserva,
        @Schema(type = "string", format = "time", example = "07:30:00") LocalTime horarioInicio,
        @Schema(type = "string", format = "time", example = "08:00:00") LocalTime horarioFim,
        UUID laboratorioId,
        @Positive(message = "quantidade de alunos deve ser positiva")
        Integer quantidadeAlunos,
        String observacao
) {
}
