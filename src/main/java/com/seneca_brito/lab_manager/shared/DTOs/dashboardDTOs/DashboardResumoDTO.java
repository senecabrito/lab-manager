package com.seneca_brito.lab_manager.shared.DTOs.dashboardDTOs;

import java.util.List;

public record DashboardResumoDTO(
        long laboratorios,
        long reservas,
        List<IndicadorDTO> reservasPorStatus,
        long reclamacoes,
        long problemasPendentes,
        List<IndicadorDTO> reclamacoesPorCategoria
) {
}
