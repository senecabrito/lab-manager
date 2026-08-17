package com.seneca_brito.lab_manager.application.Queries.laboratorio.getById;

import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioResponseDTO;
import com.seneca_brito.lab_manager.shared.mappers.LaboratorioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/laboratorios")
@RequiredArgsConstructor
public class GetByIdLaboratorio {

    private final GetByIdLaboratorioHandler handler;
    private final LaboratorioMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<LaboratorioResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toDto(handler.find(id)));
    }
}
