package com.seneca_brito.lab_manager.application.Queries.laboratorio.get;

import com.seneca_brito.lab_manager.application.Commands.laboratorio.create.CreateLaboratorioHandler;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetLaboratorioHandler {

    private final LaboratorioRepository laboratorioRepository;

    @Transactional(readOnly = true)
    public Page<Laboratorio> find(Integer capacidadeMinima, String localizacao,
                                  Set<String> recursos, Pageable pageable) {
        Specification<Laboratorio> spec = (root, query, cb) -> cb.conjunction();
        if (capacidadeMinima != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("capacidade"), capacidadeMinima));
        }
        String normalizedLocation = CreateLaboratorioHandler.normalizeOptional(localizacao);
        if (normalizedLocation != null) {
            String pattern = "%" + normalizedLocation.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("localizacao")), pattern));
        }
        for (String recurso : CreateLaboratorioHandler.normalizeResources(recursos)) {
            spec = spec.and((root, query, cb) -> cb.isMember(recurso, root.get("recursos")));
        }
        return laboratorioRepository.findAll(spec, pageable);
    }
}
