package com.seneca_brito.lab_manager.application.Commands.reserva.create;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateReservaHandler {

    private final ReservaRepository reservaRepository;


    public Reserva create(Reserva reserva) {
        return reservaRepository.save(reserva);
    }
}
