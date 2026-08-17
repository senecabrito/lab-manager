package com.seneca_brito.lab_manager.application.Commands.reserva.status;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.ConflitoEstadoException;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaStatusHandler {

    private final ReservaRepository reservaRepository;
    private final LaboratorioRepository laboratorioRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reserva approve(UUID id) {
        Reserva preliminary = reservaRepository.findDetailedById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva nao encontrada"));
        laboratorioRepository.findAllByIdForUpdate(List.of(preliminary.getLaboratorio().getId()));
        Reserva reserva = reservaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva nao encontrada"));
        requirePending(reserva);
        if (reservaRepository.existsConflito(reserva.getLaboratorio().getId(),
                reserva.getDataReserva(), reserva.getHorarioInicio(), reserva.getHorarioFim(),
                Set.of(StatusReserva.APROVADA), reserva.getId())) {
            throw new ConflitoEstadoException("Reserva conflita com horario ja aprovado");
        }
        reserva.setStatus(StatusReserva.APROVADA);
        return reservaRepository.saveAndFlush(reserva);
    }

    @Transactional
    public Reserva reject(UUID id) {
        Reserva reserva = reservaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva nao encontrada"));
        requirePending(reserva);
        reserva.setStatus(StatusReserva.REJEITADA);
        return reservaRepository.saveAndFlush(reserva);
    }

    private void requirePending(Reserva reserva) {
        if (reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new ConflitoEstadoException("Somente reserva pendente pode mudar para este estado");
        }
    }
}
