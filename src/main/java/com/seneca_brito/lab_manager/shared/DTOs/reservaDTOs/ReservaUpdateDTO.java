package com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs;

import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaUpdateDTO(
        LocalDate dataReserva,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        UUID laboratorioId,
        @Positive(message = "quantidade de alunos deve ser positiva")
        Integer quantidadeAlunos,
        String observacao
) {
}
