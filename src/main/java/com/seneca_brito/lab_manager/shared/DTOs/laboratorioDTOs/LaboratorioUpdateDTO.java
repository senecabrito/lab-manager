package com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusLaboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.TipoLaboratorio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record LaboratorioUpdateDTO(
        @Size(min = 1, max = 50, message = "nome deve ter entre 1 e 50 caracteres")
        String nome,
        @Positive(message = "capacidade deve ser positiva")
        Integer capacidade,
        @PositiveOrZero(message = "quantidade de computadores deve ser positiva ou zero")
        Integer quantidadeComputadores,
        StatusLaboratorio status,
        TipoLaboratorio tipoLaboratorio,
        @Size(max = 120, message = "localizacao deve ter no maximo 120 caracteres")
        String localizacao,
        Set<@NotBlank(message = "recurso nao pode estar em branco")
                @Size(max = 100, message = "recurso deve ter no maximo 100 caracteres") String> recursos
) {
}
