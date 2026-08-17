package com.seneca_brito.lab_manager.application.Commands.acesso;

import com.seneca_brito.lab_manager.shared.DTOs.acessoDTOs.AcessoResponseDTO;
import com.seneca_brito.lab_manager.shared.mappers.AcessoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas/{reservaId}")
@RequiredArgsConstructor
public class AcessoCommand {

    private final AcessoCommandHandler handler;
    private final AcessoMapper mapper;

    @PostMapping("/check-in")
    public ResponseEntity<AcessoResponseDTO> checkIn(@PathVariable UUID reservaId,
                                                      Authentication authentication) {
        AcessoResponseDTO response = mapper.toDto(handler.checkIn(reservaId, authentication));
        var location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/acessos/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/check-out")
    public ResponseEntity<AcessoResponseDTO> checkOut(@PathVariable UUID reservaId,
                                                       Authentication authentication) {
        return ResponseEntity.ok(mapper.toDto(handler.checkOut(reservaId, authentication)));
    }
}
