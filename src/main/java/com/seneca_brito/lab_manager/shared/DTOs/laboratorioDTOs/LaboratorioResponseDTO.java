package com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusLaboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.TipoLaboratorio;

import java.util.UUID;

public record LaboratorioResponseDTO(
        UUID id,
        String nome,
        Integer capacidade,
        Integer quantidadeComputadores,
        StatusLaboratorio status,
        TipoLaboratorio tipoLaboratorio
) {
}
