package com.seneca_brito.lab_manager.application.Commands.reclamacao.update;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReclamacaoRepository;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoStatusUpdateDTO;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoUpdateDTO;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import com.seneca_brito.lab_manager.shared.exceptions.ConflitoEstadoException;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.UserNotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateReclamacaoHandler {

    private final ReclamacaoRepository reclamacaoRepository;

    @Transactional
    public Reclamacao update(UUID id, ReclamacaoUpdateDTO dto, Authentication authentication) {
        Reclamacao reclamacao = reclamacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reclamacao nao encontrada"));
        boolean admin = isAdmin(authentication);
        if (!admin && !reclamacao.getUsuario().getEmail().equals(authentication.getName())) {
            throw new UserNotAuthorizedException("Reclamacao pertence a outro usuario");
        }
        if (!admin && reclamacao.getStatus() != StatusReclamacao.PENDENTE) {
            throw new ConflitoEstadoException("Reclamacao nao pode ser editada no estado atual");
        }
        if (dto.descricao() != null) reclamacao.setDescricao(dto.descricao());
        if (dto.categoriaProblema() != null) reclamacao.setCategoriaProblema(dto.categoriaProblema());
        return reclamacaoRepository.saveAndFlush(reclamacao);
    }

    @Transactional
    public Reclamacao updateStatus(UUID id, ReclamacaoStatusUpdateDTO dto) {
        Reclamacao reclamacao = reclamacaoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reclamacao nao encontrada"));
        if (!allowedTransitions(reclamacao.getStatus()).contains(dto.status())) {
            throw new ConflitoEstadoException("Transicao de status da reclamacao nao permitida");
        }
        reclamacao.setStatus(dto.status());
        return reclamacaoRepository.saveAndFlush(reclamacao);
    }

    private Set<StatusReclamacao> allowedTransitions(StatusReclamacao current) {
        return switch (current) {
            case PENDENTE -> Set.of(StatusReclamacao.EM_ANALISE, StatusReclamacao.CANCELADA,
                    StatusReclamacao.IMPROCEDENTE);
            case EM_ANALISE -> Set.of(StatusReclamacao.CONCLUIDA, StatusReclamacao.CANCELADA,
                    StatusReclamacao.IMPROCEDENTE);
            case CONCLUIDA, CANCELADA, IMPROCEDENTE -> Set.of();
        };
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleTypeEnum.ADMINISTRACAO.name()));
    }
}
