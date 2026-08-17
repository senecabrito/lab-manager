package com.seneca_brito.lab_manager.application.Commands.reclamacao.create;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReclamacaoRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoRequestDTO;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateReclamacaoHandler {

    private final ReclamacaoRepository reclamacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final Clock clock;

    @Transactional
    public Reclamacao create(ReclamacaoRequestDTO dto, String authenticatedEmail) {
        Usuario usuario = usuarioRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario autenticado nao encontrado"));
        Laboratorio laboratorio = laboratorioRepository.findById(dto.laboratorioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Laboratorio nao encontrado"));
        Reclamacao reclamacao = new Reclamacao();
        reclamacao.setDescricao(dto.descricao());
        reclamacao.setCategoriaProblema(dto.categoriaProblema());
        reclamacao.setDataReclamacao(LocalDateTime.now(clock));
        reclamacao.setStatus(StatusReclamacao.PENDENTE);
        reclamacao.setUsuario(usuario);
        reclamacao.setLaboratorio(laboratorio);
        return reclamacaoRepository.saveAndFlush(reclamacao);
    }
}
