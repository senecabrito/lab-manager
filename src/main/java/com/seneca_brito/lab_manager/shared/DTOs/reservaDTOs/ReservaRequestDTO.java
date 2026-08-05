package com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaRequestDTO(

        @NotNull(message = "Campo obrigatorio")
        LocalDate dataReserva,

        @NotNull(message = "Campo obrigatorio")
        LocalTime horarioInicio,

        @NotNull(message = "Campo obrigatorio")
        LocalTime horarioFim,

        @NotNull(message = "Campo obrigatorio")
        UUID usuarioId,

        @NotNull(message = "Campo obrigatorio")
        UUID laboratorioId
) {
}
