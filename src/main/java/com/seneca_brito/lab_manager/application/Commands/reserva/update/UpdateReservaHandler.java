package com.seneca_brito.lab_manager.application.Commands.reserva.update;

import com.seneca_brito.lab_manager.application.services.ReservaPolicy;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaUpdateDTO;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.ConflitoEstadoException;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.UserNotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateReservaHandler {

    private final ReservaRepository reservaRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final ReservaPolicy reservaPolicy;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reserva update(UUID id, ReservaUpdateDTO dto, Authentication authentication) {
        Reserva preliminary = reservaRepository.findDetailedById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva nao encontrada"));
        authorize(preliminary, authentication);
        UUID targetLabId = dto.laboratorioId() != null
                ? dto.laboratorioId() : preliminary.getLaboratorio().getId();
        Laboratorio laboratorio = laboratorioRepository.findAllByIdForUpdate(List.of(targetLabId))
                .stream().findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Laboratorio nao encontrado"));
        Reserva reserva = reservaRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva nao encontrada"));
        authorize(reserva, authentication);
        if (reserva.getStatus() == StatusReserva.CANCELADA
                || reserva.getStatus() == StatusReserva.REJEITADA) {
            throw new ConflitoEstadoException("Reserva encerrada nao pode ser editada");
        }
        LocalDate data = dto.dataReserva() != null ? dto.dataReserva() : reserva.getDataReserva();
        LocalTime inicio = dto.horarioInicio() != null ? dto.horarioInicio() : reserva.getHorarioInicio();
        LocalTime fim = dto.horarioFim() != null ? dto.horarioFim() : reserva.getHorarioFim();
        Integer quantidade = dto.quantidadeAlunos() != null
                ? dto.quantidadeAlunos() : reserva.getQuantidadeAlunos();
        reservaPolicy.validate(laboratorio, data, inicio, fim, quantidade);
        if (reservaRepository.existsConflito(targetLabId, data, inicio, fim,
                reservaPolicy.estadosQueBloqueiam(), reserva.getId())) {
            throw new ConflitoEstadoException("Horario indisponivel para o laboratorio");
        }
        reserva.setDataReserva(data);
        reserva.setHorarioInicio(inicio);
        reserva.setHorarioFim(fim);
        reserva.setQuantidadeAlunos(quantidade);
        reserva.setLaboratorio(laboratorio);
        if (dto.observacao() != null) reserva.setObservacao(dto.observacao());
        return reservaRepository.saveAndFlush(reserva);
    }

    private void authorize(Reserva reserva, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleTypeEnum.ADMINISTRACAO.name()));
        if (!admin && !reserva.getUsuario().getEmail().equals(authentication.getName())) {
            throw new UserNotAuthorizedException("Reserva pertence a outro usuario");
        }
    }
}
