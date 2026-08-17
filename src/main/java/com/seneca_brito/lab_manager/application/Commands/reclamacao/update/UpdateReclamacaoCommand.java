package com.seneca_brito.lab_manager.application.Commands.reclamacao.update;

import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoResponseDTO;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoStatusUpdateDTO;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoUpdateDTO;
import com.seneca_brito.lab_manager.shared.mappers.ReclamacaoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reclamacoes")
@RequiredArgsConstructor
public class UpdateReclamacaoCommand {

    private final UpdateReclamacaoHandler handler;
    private final ReclamacaoMapper mapper;

    @PatchMapping("/{id}")
    public ResponseEntity<ReclamacaoResponseDTO> update(@PathVariable UUID id,
                                                        @RequestBody @Valid ReclamacaoUpdateDTO dto,
                                                        Authentication authentication) {
        return ResponseEntity.ok(mapper.toDto(handler.update(id, dto, authentication)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReclamacaoResponseDTO> updateStatus(
            @PathVariable UUID id, @RequestBody @Valid ReclamacaoStatusUpdateDTO dto) {
        return ResponseEntity.ok(mapper.toDto(handler.updateStatus(id, dto)));
    }
}
