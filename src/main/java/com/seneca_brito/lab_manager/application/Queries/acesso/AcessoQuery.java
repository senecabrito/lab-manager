package com.seneca_brito.lab_manager.application.Queries.acesso;

import com.seneca_brito.lab_manager.domain.RegistroAcesso;
import com.seneca_brito.lab_manager.shared.DTOs.acessoDTOs.AcessoResponseDTO;
import com.seneca_brito.lab_manager.shared.ENUM.StatusAcesso;
import com.seneca_brito.lab_manager.shared.mappers.AcessoMapper;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/acessos")
@RequiredArgsConstructor
public class AcessoQuery {

    private final AcessoQueryHandler handler;
    private final AcessoMapper mapper;

    @GetMapping
    public ResponseEntity<PagedModel<AcessoResponseDTO>> getAll(
            @RequestParam(required = false) UUID reservaId,
            @RequestParam(required = false) UUID laboratorioId,
            @RequestParam(required = false) StatusAcesso status,
            @ParameterObject Pageable pageable) {
        return response(handler.find(null, reservaId, laboratorioId, status, pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<PagedModel<AcessoResponseDTO>> getMine(
            @RequestParam(required = false) UUID reservaId,
            @RequestParam(required = false) UUID laboratorioId,
            @RequestParam(required = false) StatusAcesso status,
            @ParameterObject Pageable pageable, Authentication authentication) {
        return response(handler.find(authentication.getName(), reservaId, laboratorioId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcessoResponseDTO> getById(@PathVariable UUID id,
                                                      Authentication authentication) {
        return ResponseEntity.ok(mapper.toDto(handler.findById(id, authentication)));
    }

    private ResponseEntity<PagedModel<AcessoResponseDTO>> response(Page<RegistroAcesso> page) {
        var content = page.getContent().stream().map(mapper::toDto).toList();
        Page<AcessoResponseDTO> dtoPage = new PageImpl<>(content, page.getPageable(), page.getTotalElements());
        return ResponseEntity.ok(new PagedModel<>(dtoPage));
    }
}
