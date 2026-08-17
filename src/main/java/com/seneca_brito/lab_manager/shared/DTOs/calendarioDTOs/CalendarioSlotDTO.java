package com.seneca_brito.lab_manager.shared.DTOs.calendarioDTOs;

import java.time.LocalTime;

public record CalendarioSlotDTO(LocalTime inicio, LocalTime fim, boolean ocupado) {
}
