package com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaResponseDTO(
        UUID id,
        LocalDate dataReserva,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        Integer quantidadeAlunos,
        String observacao,
        StatusReserva status,
        UUID usuarioId,
        UUID laboratorioId
) {
}
