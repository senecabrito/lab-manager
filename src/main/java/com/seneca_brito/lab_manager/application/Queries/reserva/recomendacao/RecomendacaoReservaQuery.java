package com.seneca_brito.lab_manager.application.Queries.reserva.recomendacao;

import com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs.RecomendacaoReservaRequestDTO;
import com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs.RecomendacaoReservaResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservas/recomendacoes")
@RequiredArgsConstructor
public class RecomendacaoReservaQuery {

    private final RecomendacaoReservaHandler handler;

    @PostMapping
    public ResponseEntity<RecomendacaoReservaResponseDTO> recommend(
            @RequestBody @Valid RecomendacaoReservaRequestDTO dto) {
        return ResponseEntity.ok(handler.recommend(dto));
    }
}
