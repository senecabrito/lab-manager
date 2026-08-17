package com.seneca_brito.lab_manager.application.Queries.laboratorio.get;

import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioListDTO;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.shared.mappers.LaboratorioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/laboratorios")
@RequiredArgsConstructor
public class GetLaboratorio {

    private final GetLaboratorioHandler handler;
    private final LaboratorioMapper mapper;

    @GetMapping
    public ResponseEntity<PagedModel<LaboratorioListDTO>> getAll(
            @RequestParam(required = false) Integer capacidadeMinima,
            @RequestParam(required = false) String localizacao,
            @RequestParam(required = false) Set<String> recursos,
            @ParameterObject Pageable pageable) {
        Page<Laboratorio> page = handler.find(capacidadeMinima, localizacao, recursos, pageable);
        var content = page.getContent().stream()
                .map(mapper::toListDto)
                .toList();
        Page<LaboratorioListDTO> dtoPage = new PageImpl<>(content, page.getPageable(), page.getTotalElements());
        return ResponseEntity.ok(new PagedModel<>(dtoPage));
    }
}
