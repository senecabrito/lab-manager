package com.seneca_brito.lab_manager.application.Commands.reclamacao.create;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReclamacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateReclamacaoHandler {

    private final ReclamacaoRepository reclamacaoRepository;

    public Reclamacao create(Reclamacao reclamacao) {
        return reclamacaoRepository.save(reclamacao);
    }
}
