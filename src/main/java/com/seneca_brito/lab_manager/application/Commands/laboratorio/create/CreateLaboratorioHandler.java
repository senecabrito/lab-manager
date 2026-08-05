package com.seneca_brito.lab_manager.application.Commands.laboratorio.create;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateLaboratorioHandler {

    private final LaboratorioRepository laboratorioRepository;


    public Laboratorio create(Laboratorio laboratorio) {
        return laboratorioRepository.save(laboratorio);
    }
}
