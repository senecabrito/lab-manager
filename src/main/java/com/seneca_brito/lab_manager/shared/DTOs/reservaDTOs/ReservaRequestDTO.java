package com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaRequestDTO(

        @NotNull(message = "Campo obrigatorio")
        LocalDate dataReserva,

        @NotNull(message = "Campo obrigatorio")
        @Schema(type = "string", format = "time", example = "07:30:00") LocalTime horarioInicio,

        @NotNull(message = "Campo obrigatorio")
        @Schema(type = "string", format = "time", example = "08:00:00") LocalTime horarioFim,

        @NotNull(message = "Campo obrigatorio")
        UUID laboratorioId,

        @NotNull(message = "Campo obrigatorio")
        @Positive(message = "quantidade de alunos deve ser positiva")
        Integer quantidadeAlunos,

        String observacao
) {
}
