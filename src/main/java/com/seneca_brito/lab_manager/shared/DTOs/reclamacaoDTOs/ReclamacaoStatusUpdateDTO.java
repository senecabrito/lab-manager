package com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs;

import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import jakarta.validation.constraints.NotNull;

public record ReclamacaoStatusUpdateDTO(
        @NotNull(message = "Campo obrigatorio") StatusReclamacao status
) {
}
