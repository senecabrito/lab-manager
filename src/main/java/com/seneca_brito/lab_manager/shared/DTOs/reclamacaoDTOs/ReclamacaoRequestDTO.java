package com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.CategoriaProblema;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReclamacaoRequestDTO(

        @NotNull(message = "Campo obrigatorio")
        String descricao,

        @NotNull(message = "Campo obrigatorio")
        CategoriaProblema categoriaProblema,

        @NotNull(message = "Campo obrigatorio")
        LocalDateTime DataReclamacao,

        @NotNull(message = "Campo obrigatorio")
        StatusReclamacao status,

        @NotNull(message = "Campo obrigatorio")
        UUID idUsuario,

        @NotNull(message = "Campo obrigatorio")
        UUID idLaboratorio
) {

}
