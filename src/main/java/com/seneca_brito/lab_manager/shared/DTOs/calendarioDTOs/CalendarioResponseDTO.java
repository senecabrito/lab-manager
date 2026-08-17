package com.seneca_brito.lab_manager.shared.DTOs.calendarioDTOs;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CalendarioResponseDTO(
        UUID laboratorioId,
        LocalDate data,
        String timezone,
        List<CalendarioSlotDTO> slots
) {
}
