package com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusLaboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.TipoLaboratorio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record LaboratorioRequestDTO(

        @NotBlank(message = "Campo obrigatorio")
        String nome,

        @NotNull(message = "Campo obrigatorio")
        @Positive(message = "Insira uma capacidade valida para o laboratorio")
        Integer capacidade,

        @NotNull(message = "Campo obrigatorio")
        @PositiveOrZero(message = "Insira uma quantidade valida de computadores desse laboratorio")
        Integer quantidadeComputadores,

        @NotNull(message = "Campo obrigatorio")
        StatusLaboratorio status,

        @NotNull(message = "Campo obrigatorio")
        TipoLaboratorio tipoLaboratorio
) {
}
