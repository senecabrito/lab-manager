package com.seneca_brito.lab_manager.application.Queries.laboratorio.getById;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetByIdLaboratorioHandler {

    private final LaboratorioRepository laboratorioRepository;

    @Transactional(readOnly = true)
    public Laboratorio find(UUID id) {
        return laboratorioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Laboratorio nao encontrado"));
    }
}
