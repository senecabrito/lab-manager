package com.seneca_brito.lab_manager.application.Commands.inventario;

import com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs.InventarioRequestDTO;
import com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs.InventarioResponseDTO;
import com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs.InventarioUpdateDTO;
import com.seneca_brito.lab_manager.shared.mappers.InventarioMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class InventarioCommand {

    private final InventarioCommandHandler handler;
    private final InventarioMapper mapper;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid InventarioRequestDTO dto) {
        UUID id = handler.create(dto).getId();
        return ResponseEntity.created(URI.create("/api/v1/inventario/" + id)).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<InventarioResponseDTO> update(@PathVariable UUID id,
                                                        @RequestBody @Valid InventarioUpdateDTO dto) {
        return ResponseEntity.ok(mapper.toDto(handler.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        handler.delete(id);
        return ResponseEntity.noContent().build();
    }
}
