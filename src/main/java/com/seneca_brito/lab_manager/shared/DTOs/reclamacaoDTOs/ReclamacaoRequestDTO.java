package com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.CategoriaProblema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReclamacaoRequestDTO(

        @NotBlank(message = "Campo obrigatorio")
        String descricao,

        @NotNull(message = "Campo obrigatorio")
        CategoriaProblema categoriaProblema,

        @NotNull(message = "Campo obrigatorio")
        UUID laboratorioId
) {

}
