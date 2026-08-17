package com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
        UUID laboratorioId,

        @NotNull(message = "Campo obrigatorio")
        @Positive(message = "quantidade de alunos deve ser positiva")
        Integer quantidadeAlunos,

        String observacao
) {
}
