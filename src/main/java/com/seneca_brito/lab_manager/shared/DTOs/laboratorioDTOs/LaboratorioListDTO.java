package com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusLaboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.TipoLaboratorio;

import java.util.Set;
import java.util.UUID;

public record LaboratorioListDTO(
        UUID id,
        String nome,
        Integer capacidade,
        String localizacao,
        StatusLaboratorio status,
        TipoLaboratorio tipoLaboratorio,
        Set<String> recursos
) {
}
