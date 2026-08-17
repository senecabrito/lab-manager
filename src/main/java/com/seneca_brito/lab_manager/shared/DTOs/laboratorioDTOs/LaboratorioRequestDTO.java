package com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusLaboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.TipoLaboratorio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Set;

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
        TipoLaboratorio tipoLaboratorio,

        @Size(max = 120, message = "localizacao deve ter no maximo 120 caracteres")
        String localizacao,

        Set<@NotBlank(message = "recurso nao pode estar em branco")
                @Size(max = 100, message = "recurso deve ter no maximo 100 caracteres") String> recursos
) {
}
