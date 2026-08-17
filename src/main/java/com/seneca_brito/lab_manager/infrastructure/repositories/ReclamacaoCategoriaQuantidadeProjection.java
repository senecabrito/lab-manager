package com.seneca_brito.lab_manager.infrastructure.repositories;

import com.seneca_brito.lab_manager.shared.ENUM.CategoriaProblema;

public interface ReclamacaoCategoriaQuantidadeProjection {
    CategoriaProblema getCategoria();
    long getQuantidade();
}
