package com.seneca_brito.lab_manager.application.Queries.inventario;

import com.seneca_brito.lab_manager.domain.InventarioItem;
import com.seneca_brito.lab_manager.infrastructure.repositories.InventarioRepository;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventarioQueryHandler {

    private final InventarioRepository repository;

    @Transactional(readOnly = true)
    public Page<InventarioItem> findAll(UUID laboratorioId, Pageable pageable) {
        return repository.findAllDetailed(laboratorioId, pageable);
    }

    @Transactional(readOnly = true)
    public InventarioItem findById(UUID id) {
        return repository.findDetailedById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de inventario nao encontrado"));
    }
}
