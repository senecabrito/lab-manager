package com.seneca_brito.lab_manager.application.Commands.reserva.delete;

import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaResponseDTO;
import com.seneca_brito.lab_manager.shared.mappers.ReservaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class DeleteReservaCommand {

    private final DeleteReservaHandler handler;
    private final ReservaMapper mapper;

    @PatchMapping("/{id}/cancelamento")
    public ResponseEntity<ReservaResponseDTO> cancel(@PathVariable UUID id,
                                                     Authentication authentication) {
        return ResponseEntity.ok(mapper.toDto(handler.cancel(id, authentication)));
    }
}
