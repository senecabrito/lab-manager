package com.seneca_brito.lab_manager.application.Queries.inventario;

import com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs.InventarioResponseDTO;
import com.seneca_brito.lab_manager.shared.mappers.InventarioMapper;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class InventarioQuery {

    private final InventarioQueryHandler handler;
    private final InventarioMapper mapper;

    @GetMapping
    public ResponseEntity<PagedModel<InventarioResponseDTO>> getAll(
            @RequestParam(required = false) UUID laboratorioId,
            @ParameterObject Pageable pageable) {
        var page = handler.findAll(laboratorioId, pageable);
        var content = page.getContent().stream().map(mapper::toDto).toList();
        Page<InventarioResponseDTO> dtoPage = new PageImpl<>(content, page.getPageable(),
                page.getTotalElements());
        return ResponseEntity.ok(new PagedModel<>(dtoPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventarioResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toDto(handler.findById(id)));
    }
}
