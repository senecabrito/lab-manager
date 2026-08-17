package com.seneca_brito.lab_manager.application.Commands.laboratorio.update;

import com.seneca_brito.lab_manager.application.Commands.laboratorio.create.CreateLaboratorioHandler;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioUpdateDTO;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateLaboratorioHandler {

    private final LaboratorioRepository laboratorioRepository;

    @Transactional
    public Laboratorio update(UUID id, LaboratorioUpdateDTO dto) {
        Laboratorio laboratorio = laboratorioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Laboratorio nao encontrado"));
        if (dto.nome() != null) laboratorio.setNome(dto.nome());
        if (dto.capacidade() != null) laboratorio.setCapacidade(dto.capacidade());
        if (dto.quantidadeComputadores() != null) {
            laboratorio.setQuantidadeComputadores(dto.quantidadeComputadores());
        }
        if (dto.status() != null) laboratorio.setStatus(dto.status());
        if (dto.tipoLaboratorio() != null) laboratorio.setTipoLaboratorio(dto.tipoLaboratorio());
        if (dto.localizacao() != null) {
            laboratorio.setLocalizacao(CreateLaboratorioHandler.normalizeOptional(dto.localizacao()));
        }
        if (dto.recursos() != null) {
            laboratorio.setRecursos(CreateLaboratorioHandler.normalizeResources(dto.recursos()));
        }
        return laboratorioRepository.saveAndFlush(laboratorio);
    }
}
