package com.seneca_brito.lab_manager.application.Queries.calendario;

import com.seneca_brito.lab_manager.shared.DTOs.calendarioDTOs.CalendarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/laboratorios")
@RequiredArgsConstructor
public class GetCalendario {

    private final GetCalendarioHandler handler;

    @GetMapping("/{id}/calendario")
    public ResponseEntity<CalendarioResponseDTO> get(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(handler.get(id, data));
    }
}
