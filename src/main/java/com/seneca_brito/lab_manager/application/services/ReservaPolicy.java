package com.seneca_brito.lab_manager.application.services;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReservaPolicy {

    private final ReservaSettings settings;
    private final Clock clock;

    public void validate(Laboratorio laboratorio, LocalDate data, LocalTime inicio,
                         LocalTime fim, Integer quantidadeAlunos) {
        if (data == null || inicio == null || fim == null || quantidadeAlunos == null) {
            throw new RegraNegocioException("Dados obrigatorios da reserva nao informados");
        }
        if (!inicio.isBefore(fim)) {
            throw new RegraNegocioException("Horario inicial deve ser anterior ao horario final");
        }
        if (inicio.isBefore(settings.openingTime()) || fim.isAfter(settings.closingTime())) {
            throw new RegraNegocioException("Reserva fora do horario de funcionamento");
        }
        long inicioOffset = Duration.between(settings.openingTime(), inicio).toMinutes();
        long fimOffset = Duration.between(settings.openingTime(), fim).toMinutes();
        if (inicioOffset % settings.slotMinutes() != 0 || fimOffset % settings.slotMinutes() != 0) {
            throw new RegraNegocioException("Horarios devem respeitar a granularidade da agenda");
        }
        if (quantidadeAlunos <= 0) {
            throw new RegraNegocioException("Quantidade de alunos deve ser positiva");
        }
        if (quantidadeAlunos > laboratorio.getCapacidade()) {
            throw new RegraNegocioException("Quantidade de alunos excede a capacidade do laboratorio");
        }
        var inicioReserva = ZonedDateTime.of(data, inicio, settings.zoneId()).toInstant();
        if (inicioReserva.isBefore(clock.instant().plus(Duration.ofHours(settings.minimumAdvanceHours())))) {
            throw new RegraNegocioException("Reserva deve possuir antecedencia minima de "
                    + settings.minimumAdvanceHours() + " horas");
        }
    }

    public Set<StatusReserva> estadosQueBloqueiam() {
        return StatusReserva.estadosQueBloqueiam();
    }

    public ReservaSettings settings() {
        return settings;
    }
}
