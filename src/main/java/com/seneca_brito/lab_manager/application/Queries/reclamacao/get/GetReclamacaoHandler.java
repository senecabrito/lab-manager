package com.seneca_brito.lab_manager.application.Queries.reclamacao.get;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReclamacaoRepository;
import com.seneca_brito.lab_manager.shared.ENUM.CategoriaProblema;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetReclamacaoHandler {

    private final ReclamacaoRepository reclamacaoRepository;

    @Transactional(readOnly = true)
    public Page<Reclamacao> find(String ownerEmail, UUID laboratorioId,
                                 StatusReclamacao status, CategoriaProblema categoria,
                                 Pageable pageable) {
        Specification<Reclamacao> spec = (root, query, cb) -> cb.conjunction();
        if (ownerEmail != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("usuario").get("email"), ownerEmail));
        }
        if (laboratorioId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("laboratorio").get("id"), laboratorioId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (categoria != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoriaProblema"), categoria));
        }
        return reclamacaoRepository.findAll(spec, pageable);
    }
}
