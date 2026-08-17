package com.seneca_brito.lab_manager.application.Commands.reserva.create;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaRequestDTO;
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
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class CreateReservaCommand {

    private final CreateReservaHandler reservaHandler;

    @PostMapping
    public ResponseEntity<Void> createReserva(@RequestBody @Valid ReservaRequestDTO reservaDTO,
                                               Authentication authentication){
        Reserva response = reservaHandler.create(reservaDTO, authentication.getName());
        String idReserva = response.getId().toString();

        return ResponseEntity.created(URI.create("/api/v1/reservas/" + idReserva)).build();
    }
}
