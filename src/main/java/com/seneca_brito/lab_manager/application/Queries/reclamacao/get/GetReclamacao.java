package com.seneca_brito.lab_manager.application.Queries.reclamacao.get;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoResponseDTO;
import com.seneca_brito.lab_manager.shared.ENUM.CategoriaProblema;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import com.seneca_brito.lab_manager.shared.mappers.ReclamacaoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reclamacoes")
@RequiredArgsConstructor
public class GetReclamacao {

    private final GetReclamacaoHandler handler;
    private final ReclamacaoMapper mapper;

    @GetMapping
    public ResponseEntity<PagedModel<ReclamacaoResponseDTO>> getAll(
            @RequestParam(required = false) UUID laboratorioId,
            @RequestParam(required = false) StatusReclamacao status,
            @RequestParam(required = false) CategoriaProblema categoria,
            Pageable pageable) {
        return response(handler.find(null, laboratorioId, status, categoria, pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<PagedModel<ReclamacaoResponseDTO>> getMine(
            @RequestParam(required = false) UUID laboratorioId,
            @RequestParam(required = false) StatusReclamacao status,
            @RequestParam(required = false) CategoriaProblema categoria,
            Pageable pageable, Authentication authentication) {
        return response(handler.find(authentication.getName(), laboratorioId, status, categoria, pageable));
    }

    private ResponseEntity<PagedModel<ReclamacaoResponseDTO>> response(Page<Reclamacao> page) {
        var content = page.getContent().stream().map(mapper::toDto).toList();
        Page<ReclamacaoResponseDTO> dtoPage =
                new PageImpl<>(content, page.getPageable(), page.getTotalElements());
        return ResponseEntity.ok(new PagedModel<>(dtoPage));
    }
}
