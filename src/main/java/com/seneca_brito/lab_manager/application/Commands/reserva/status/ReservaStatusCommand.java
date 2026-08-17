package com.seneca_brito.lab_manager.application.Commands.reserva.status;

import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaResponseDTO;
import com.seneca_brito.lab_manager.shared.mappers.ReservaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaStatusCommand {

    private final ReservaStatusHandler handler;
    private final ReservaMapper mapper;

    @PatchMapping("/{id}/aprovacao")
    public ResponseEntity<ReservaResponseDTO> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toDto(handler.approve(id)));
    }

    @PatchMapping("/{id}/rejeicao")
    public ResponseEntity<ReservaResponseDTO> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toDto(handler.reject(id)));
    }
}
