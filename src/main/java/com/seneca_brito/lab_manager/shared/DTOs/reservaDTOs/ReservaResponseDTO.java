package com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservaResponseDTO(
        UUID id,
        LocalDate dataReserva,
        @Schema(type = "string", format = "time", example = "07:30:00") LocalTime horarioInicio,
        @Schema(type = "string", format = "time", example = "08:00:00") LocalTime horarioFim,
        Integer quantidadeAlunos,
        String observacao,
        StatusReserva status,
        UUID usuarioId,
        UUID laboratorioId
) {
}
