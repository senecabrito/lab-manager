package com.seneca_brito.lab_manager.application.Queries.reclamacao.getById;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReclamacaoRepository;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.UserNotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetByIdReclamacaoHandler {

    private final ReclamacaoRepository reclamacaoRepository;

    @Transactional(readOnly = true)
    public Reclamacao find(UUID id, Authentication authentication) {
        Reclamacao reclamacao = reclamacaoRepository.findDetailedById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reclamacao nao encontrada"));
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleTypeEnum.ADMINISTRACAO.name()));
        if (!admin && !reclamacao.getUsuario().getEmail().equals(authentication.getName())) {
            throw new UserNotAuthorizedException("Reclamacao pertence a outro usuario");
        }
        return reclamacao;
    }
}
