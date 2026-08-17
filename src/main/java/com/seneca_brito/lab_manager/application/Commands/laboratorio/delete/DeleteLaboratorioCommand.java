package com.seneca_brito.lab_manager.application.Commands.laboratorio.delete;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/laboratorios")
@RequiredArgsConstructor
public class DeleteLaboratorioCommand {

    private final DeleteLaboratorioHandler handler;

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        handler.delete(id);
        return ResponseEntity.noContent().build();
    }
}
