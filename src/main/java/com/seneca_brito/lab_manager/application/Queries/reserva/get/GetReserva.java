package com.seneca_brito.lab_manager.application.Queries.reserva.get;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaResponseDTO;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.mappers.ReservaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class GetReserva {

    private final GetReservaHandler handler;
    private final ReservaMapper mapper;

    @GetMapping
    public ResponseEntity<PagedModel<ReservaResponseDTO>> getAll(
            @RequestParam(required = false) UUID laboratorioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) StatusReserva status,
            Pageable pageable) {
        return response(handler.find(null, laboratorioId, data, status, pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<PagedModel<ReservaResponseDTO>> getMine(
            @RequestParam(required = false) UUID laboratorioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) StatusReserva status,
            Pageable pageable, Authentication authentication) {
        return response(handler.find(authentication.getName(), laboratorioId, data, status, pageable));
    }

    private ResponseEntity<PagedModel<ReservaResponseDTO>> response(Page<Reserva> page) {
        var content = page.getContent().stream().map(mapper::toDto).toList();
        Page<ReservaResponseDTO> dtoPage = new PageImpl<>(content, page.getPageable(), page.getTotalElements());
        return ResponseEntity.ok(new PagedModel<>(dtoPage));
    }
}
