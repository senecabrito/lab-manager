package com.seneca_brito.lab_manager.application.Commands.laboratorio.update;

import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioResponseDTO;
import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioUpdateDTO;
import com.seneca_brito.lab_manager.shared.mappers.LaboratorioMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/laboratorios")
@RequiredArgsConstructor
public class UpdateLaboratorioCommand {

    private final UpdateLaboratorioHandler handler;
    private final LaboratorioMapper mapper;

    @PatchMapping("/{id}")
    public ResponseEntity<LaboratorioResponseDTO> update(@PathVariable UUID id,
                                                         @RequestBody @Valid LaboratorioUpdateDTO dto) {
        return ResponseEntity.ok(mapper.toDto(handler.update(id, dto)));
    }
}
