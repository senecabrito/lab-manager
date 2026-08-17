package com.seneca_brito.lab_manager.application.Commands.reclamacao.delete;

import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoResponseDTO;
import com.seneca_brito.lab_manager.shared.mappers.ReclamacaoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reclamacoes")
@RequiredArgsConstructor
public class DeleteReclamacaoCommand {

    private final DeleteReclamacaoHandler handler;
    private final ReclamacaoMapper mapper;

    @PatchMapping("/{id}/cancelamento")
    public ResponseEntity<ReclamacaoResponseDTO> cancel(@PathVariable UUID id,
                                                        Authentication authentication) {
        return ResponseEntity.ok(mapper.toDto(handler.cancel(id, authentication)));
    }
}
