package com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.CategoriaProblema;
import jakarta.validation.constraints.Size;

public record ReclamacaoUpdateDTO(
        @Size(min = 1, message = "descricao nao pode estar vazia")
        String descricao,
        CategoriaProblema categoriaProblema
) {
}
