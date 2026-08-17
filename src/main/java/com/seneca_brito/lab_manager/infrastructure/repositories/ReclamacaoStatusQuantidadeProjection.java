package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;

public interface ReclamacaoStatusQuantidadeProjection {
    StatusReclamacao getStatus();
    long getQuantidade();
}
