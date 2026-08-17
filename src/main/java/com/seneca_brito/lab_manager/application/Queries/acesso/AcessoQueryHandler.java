package com.seneca_brito.lab_manager.application.Queries.acesso;

import com.seneca_brito.lab_manager.domain.RegistroAcesso;
import com.seneca_brito.lab_manager.infrastructure.repositories.RegistroAcessoRepository;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.ENUM.StatusAcesso;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.UserNotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcessoQueryHandler {

    private final RegistroAcessoRepository acessoRepository;

    @Transactional(readOnly = true)
    public Page<RegistroAcesso> find(String ownerEmail, UUID reservaId, UUID laboratorioId,
                                     StatusAcesso status, Pageable pageable) {
        Specification<RegistroAcesso> spec = (root, query, cb) -> cb.conjunction();
        if (ownerEmail != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("reserva").get("usuario").get("email"), ownerEmail));
        }
        if (reservaId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("reserva").get("id"), reservaId));
        }
        if (laboratorioId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("reserva").get("laboratorio").get("id"), laboratorioId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return acessoRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public RegistroAcesso findById(UUID id, Authentication authentication) {
        RegistroAcesso acesso = acessoRepository.findDetailedById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Acesso nao encontrado"));
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(RoleTypeEnum.ADMINISTRACAO.name()));
        if (!admin && !acesso.getReserva().getUsuario().getEmail().equals(authentication.getName())) {
            throw new UserNotAuthorizedException("Acesso pertence a outro usuario");
        }
        return acesso;
    }
}
