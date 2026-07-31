package com.seneca_brito.lab_manager.application.Commands.reserva.create;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaRequestDTO;
import com.seneca_brito.lab_manager.shared.mappers.ReservaMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class CreateReservaCommand {

    private final CreateReservaHandler reservaHandler;
    private final ReservaMapper reservaMapper;

    @PostMapping
    public ResponseEntity<Void> createReserva(@RequestBody @Valid ReservaRequestDTO reservaDTO){
        Reserva reserva = reservaMapper.toModel(reservaDTO);

        Reserva response = reservaHandler.create(reserva);
        String idReserva = response.getId().toString();

        return ResponseEntity.created(URI.create(idReserva)).build();
    }
}
