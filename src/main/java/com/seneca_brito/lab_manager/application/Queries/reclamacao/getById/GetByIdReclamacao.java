package com.seneca_brito.lab_manager.application.Queries.reclamacao.getById;

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
public class GetByIdReclamacao {

    private final GetByIdReclamacaoHandler handler;
    private final ReclamacaoMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<ReclamacaoResponseDTO> getById(@PathVariable UUID id,
                                                         Authentication authentication) {
        return ResponseEntity.ok(mapper.toDto(handler.find(id, authentication)));
    }
}
