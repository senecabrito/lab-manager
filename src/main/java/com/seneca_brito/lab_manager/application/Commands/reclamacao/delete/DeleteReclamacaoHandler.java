package com.seneca_brito.lab_manager.application.Commands.reclamacao.delete;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReclamacaoRepository;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import com.seneca_brito.lab_manager.shared.exceptions.ConflitoEstadoException;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.UserNotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteReclamacaoHandler {

    private final ReclamacaoRepository reclamacaoRepository;

    @Transactional
    public Reclamacao cancel(UUID id, Authentication authentication) {
        Reclamacao reclamacao = reclamacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reclamacao nao encontrada"));
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleTypeEnum.ADMINISTRACAO.name()));
        if (!admin && !reclamacao.getUsuario().getEmail().equals(authentication.getName())) {
            throw new UserNotAuthorizedException("Reclamacao pertence a outro usuario");
        }
        if (reclamacao.getStatus() != StatusReclamacao.PENDENTE) {
            throw new ConflitoEstadoException("Reclamacao nao pode ser cancelada no estado atual");
        }
        reclamacao.setStatus(StatusReclamacao.CANCELADA);
        return reclamacaoRepository.saveAndFlush(reclamacao);
    }
}
