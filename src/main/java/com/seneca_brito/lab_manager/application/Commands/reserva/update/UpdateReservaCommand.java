package com.seneca_brito.lab_manager.application.Commands.reserva.update;

import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaResponseDTO;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaUpdateDTO;
import com.seneca_brito.lab_manager.shared.mappers.ReservaMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class UpdateReservaCommand {

    private final UpdateReservaHandler handler;
    private final ReservaMapper mapper;

    @PatchMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> update(@PathVariable UUID id,
                                                     @RequestBody @Valid ReservaUpdateDTO dto,
                                                     Authentication authentication) {
        return ResponseEntity.ok(mapper.toDto(handler.update(id, dto, authentication)));
    }
}
