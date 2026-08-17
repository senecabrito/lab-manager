package com.seneca_brito.lab_manager.application.Queries.reserva.getById;

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
public class GetByIdReserva {

    private final GetByIdReservaHandler handler;
    private final ReservaMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> getById(@PathVariable UUID id,
                                                      Authentication authentication) {
        return ResponseEntity.ok(mapper.toDto(handler.find(id, authentication)));
    }
}
