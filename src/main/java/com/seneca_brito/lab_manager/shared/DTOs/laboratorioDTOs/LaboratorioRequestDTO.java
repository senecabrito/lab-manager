package com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusLaboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.TipoLaboratorio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record LaboratorioRequestDTO(

        @NotBlank(message = "Campo obrigatorio")
        String nome,

        @NotBlank(message = "Campo obrigatorio")
        Integer capacidade,

        @NotBlank(message = "Campo obrigatorio")
        @PositiveOrZero(message = "Insira uma quantidade valida de computadores desse laboratorio")
        Integer quantidadeComputadores,

        @NotBlank(message = "Campo obrigatorio")
        StatusLaboratorio status,

        @NotBlank(message = "Campo obrigatorio")
        TipoLaboratorio tipoLaboratorio
) {
}
