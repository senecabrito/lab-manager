package com.seneca_brito.lab_manager.application.Queries.calendario;

import com.seneca_brito.lab_manager.application.services.ReservaPolicy;
import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.DTOs.calendarioDTOs.CalendarioResponseDTO;
import com.seneca_brito.lab_manager.shared.DTOs.calendarioDTOs.CalendarioSlotDTO;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCalendarioHandler {

    private final LaboratorioRepository laboratorioRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaPolicy reservaPolicy;

    @Transactional(readOnly = true)
    public CalendarioResponseDTO get(UUID laboratorioId, LocalDate data) {
        if (!laboratorioRepository.existsById(laboratorioId)) {
            throw new RecursoNaoEncontradoException("Laboratorio nao encontrado");
        }
        List<Reserva> ocupacoes = reservaRepository.findAgenda(
                laboratorioId, data, reservaPolicy.estadosQueBloqueiam());
        List<CalendarioSlotDTO> slots = new ArrayList<>();
        LocalTime cursor = reservaPolicy.settings().openingTime();
        while (cursor.isBefore(reservaPolicy.settings().closingTime())) {
            LocalTime fim = cursor.plusMinutes(reservaPolicy.settings().slotMinutes());
            if (fim.isAfter(reservaPolicy.settings().closingTime())) {
                fim = reservaPolicy.settings().closingTime();
            }
            LocalTime slotInicio = cursor;
            LocalTime slotFim = fim;
            boolean ocupado = ocupacoes.stream().anyMatch(reserva ->
                    reserva.getHorarioInicio().isBefore(slotFim)
                            && reserva.getHorarioFim().isAfter(slotInicio));
            slots.add(new CalendarioSlotDTO(slotInicio, slotFim, ocupado));
            cursor = fim;
        }
        return new CalendarioResponseDTO(laboratorioId, data,
                reservaPolicy.settings().zoneId().getId(), List.copyOf(slots));
    }
}
