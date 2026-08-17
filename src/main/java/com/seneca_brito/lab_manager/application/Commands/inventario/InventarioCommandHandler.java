package com.seneca_brito.lab_manager.application.Commands.inventario;

import com.seneca_brito.lab_manager.domain.InventarioItem;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.InventarioRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs.InventarioRequestDTO;
import com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs.InventarioUpdateDTO;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventarioCommandHandler {

    private final InventarioRepository inventarioRepository;
    private final LaboratorioRepository laboratorioRepository;

    @Transactional
    public InventarioItem create(InventarioRequestDTO dto) {
        InventarioItem item = new InventarioItem();
        item.setNome(normalizeName(dto.nome()));
        item.setQuantidadeDisponivel(dto.quantidadeDisponivel());
        item.setQuantidadeIndisponivel(dto.quantidadeIndisponivel());
        item.setLaboratorio(findLaboratory(dto.laboratorioId()));
        return inventarioRepository.saveAndFlush(item);
    }

    @Transactional
    public InventarioItem update(UUID id, InventarioUpdateDTO dto) {
        InventarioItem item = inventarioRepository.findDetailedById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de inventario nao encontrado"));
        if (dto.nome() != null) item.setNome(normalizeName(dto.nome()));
        if (dto.quantidadeDisponivel() != null) {
            item.setQuantidadeDisponivel(dto.quantidadeDisponivel());
        }
        if (dto.quantidadeIndisponivel() != null) {
            item.setQuantidadeIndisponivel(dto.quantidadeIndisponivel());
        }
        if (dto.laboratorioId() != null) item.setLaboratorio(findLaboratory(dto.laboratorioId()));
        return inventarioRepository.saveAndFlush(item);
    }

    @Transactional
    public void delete(UUID id) {
        InventarioItem item = inventarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item de inventario nao encontrado"));
        inventarioRepository.delete(item);
        inventarioRepository.flush();
    }

    private Laboratorio findLaboratory(UUID id) {
        return laboratorioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Laboratorio nao encontrado"));
    }

    private static String normalizeName(String value) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new RegraNegocioException("Nome do item deve ser informado");
        }
        return normalized;
    }
}
