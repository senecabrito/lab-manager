package com.seneca_brito.lab_manager.shared.DTOs.dashboardDTOs;

import java.time.LocalDate;
import java.util.List;

public record HistoricoDTO(
        LocalDate dataInicial,
        LocalDate dataFinal,
        List<IndicadorDTO> reservasPorStatus,
        List<IndicadorDTO> reclamacoesPorStatus
) {
}
