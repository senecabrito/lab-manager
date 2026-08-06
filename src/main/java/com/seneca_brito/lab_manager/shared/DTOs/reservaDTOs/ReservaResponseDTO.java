package com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaResponseDTO(
        UUID id,
        LocalDate dataReserva,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        UUID usuarioId,
        UUID laboratorioId
) {
}
