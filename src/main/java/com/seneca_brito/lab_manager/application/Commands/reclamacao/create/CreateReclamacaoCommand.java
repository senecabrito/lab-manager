package com.seneca_brito.lab_manager.application.Commands.reclamacao.create;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/reclamacoes")
@RequiredArgsConstructor
public class CreateReclamacaoCommand {

    private final CreateReclamacaoHandler reclamacaoHandler;

    @PostMapping
    public ResponseEntity<Void> createReclamacao(@RequestBody @Valid ReclamacaoRequestDTO reclamacaoDTO,
                                                  Authentication authentication){
        Reclamacao response = reclamacaoHandler.create(reclamacaoDTO, authentication.getName());
        String idReclamacao = response.getId().toString();

        return ResponseEntity.created(URI.create("/api/v1/reclamacoes/" + idReclamacao)).build();
    }
}
