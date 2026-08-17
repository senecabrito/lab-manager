package com.seneca_brito.lab_manager.application.Commands.laboratorio.delete;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteLaboratorioHandler {

    private final LaboratorioRepository laboratorioRepository;

    @Transactional
    public void delete(UUID id) {
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Laboratorio nao encontrado"));
        laboratorioRepository.delete(laboratorio);
        laboratorioRepository.flush();
    }
}
