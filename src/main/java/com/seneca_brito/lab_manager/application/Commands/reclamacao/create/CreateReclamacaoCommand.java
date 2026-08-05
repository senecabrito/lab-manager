package com.seneca_brito.lab_manager.application.Commands.reclamacao.create;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoRequestDTO;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaRequestDTO;
import com.seneca_brito.lab_manager.shared.mappers.ReclamacaoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/reclamacoes")
@RequiredArgsConstructor
public class CreateReclamacaoCommand {

    private final CreateReclamacaoHandler reclamacaoHandler;
    private final ReclamacaoMapper  reclamacaoMapper;

    @PostMapping
    public ResponseEntity<Void> createReserva(@RequestBody @Valid ReclamacaoRequestDTO reclamacaoDTO){
        Reclamacao reclamacao = reclamacaoMapper.toModel(reclamacaoDTO);

        Reclamacao response = reclamacaoHandler.create(reclamacao);
        String idReclamacao = response.getId().toString();

        return ResponseEntity.created(URI.create(idReclamacao)).build();
    }
}
