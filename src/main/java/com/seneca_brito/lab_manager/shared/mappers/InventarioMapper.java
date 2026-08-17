package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.InventarioItem;
import com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs.InventarioResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {

    public InventarioResponseDTO toDto(InventarioItem item) {
        return new InventarioResponseDTO(item.getId(), item.getNome(), item.getQuantidadeDisponivel(),
                item.getQuantidadeIndisponivel(), item.getLaboratorio().getId(),
                item.getLaboratorio().getNome());
    }
}
