package com.seneca_brito.lab_manager.shared.DTOs.dashboardDTOs;

import java.math.BigDecimal;
import java.util.UUID;

public record UtilizacaoLaboratorioDTO(
        UUID laboratorioId,
        String laboratorio,
        long minutosOcupados,
        long minutosDisponiveis,
        BigDecimal percentualUtilizacao
) {
}
