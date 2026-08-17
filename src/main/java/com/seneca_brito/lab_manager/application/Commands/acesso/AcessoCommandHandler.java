package com.seneca_brito.lab_manager.application.Commands.acesso;

import com.seneca_brito.lab_manager.application.services.ReservaSettings;
import com.seneca_brito.lab_manager.domain.RegistroAcesso;
import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.infrastructure.repositories.RegistroAcessoRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.ENUM.StatusAcesso;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.ConflitoEstadoException;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import com.seneca_brito.lab_manager.shared.exceptions.UserNotAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcessoCommandHandler {

    private final ReservaRepository reservaRepository;
    private final RegistroAcessoRepository acessoRepository;
    private final ReservaSettings reservaSettings;
    private final Clock clock;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RegistroAcesso checkIn(UUID reservaId, Authentication authentication) {
        Reserva reserva = reservaRepository.findByIdForUpdate(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva nao encontrada"));
        authorize(reserva, authentication);
        if (reserva.getStatus() != StatusReserva.APROVADA) {
            throw new RegraNegocioException("Check-in exige uma reserva APROVADA");
        }
        if (acessoRepository.existsByReservaId(reservaId)) {
            throw new ConflitoEstadoException("Check-in ja registrado para a reserva");
        }

        Instant now = clock.instant();
        Instant inicio = reserva.getDataReserva().atTime(reserva.getHorarioInicio())
                .atZone(reservaSettings.zoneId()).toInstant();
        Instant fim = reserva.getDataReserva().atTime(reserva.getHorarioFim())
                .atZone(reservaSettings.zoneId()).toInstant();
        if (now.isBefore(inicio) || !now.isBefore(fim)) {
            throw new RegraNegocioException("Check-in permitido somente durante o horario da reserva");
        }

        RegistroAcesso acesso = new RegistroAcesso();
        acesso.setReserva(reserva);
        acesso.setCheckIn(now);
        acesso.setStatus(StatusAcesso.EM_ANDAMENTO);
        return acessoRepository.saveAndFlush(acesso);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RegistroAcesso checkOut(UUID reservaId, Authentication authentication) {
        Reserva reserva = reservaRepository.findByIdForUpdate(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva nao encontrada"));
        authorize(reserva, authentication);
        RegistroAcesso acesso = acessoRepository.findByReservaIdForUpdate(reservaId)
                .orElseThrow(() -> new ConflitoEstadoException("Check-in ainda nao registrado"));
        if (acesso.getStatus() == StatusAcesso.FINALIZADO || acesso.getCheckOut() != null) {
            throw new ConflitoEstadoException("Check-out ja registrado para a reserva");
        }

        Instant now = clock.instant();
        if (now.isBefore(acesso.getCheckIn())) {
            throw new RegraNegocioException("Check-out nao pode anteceder o check-in");
        }
        acesso.setCheckOut(now);
        acesso.setStatus(StatusAcesso.FINALIZADO);
        return acessoRepository.saveAndFlush(acesso);
    }

    private void authorize(Reserva reserva, Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(RoleTypeEnum.ADMINISTRACAO.name()));
        if (!admin && !reserva.getUsuario().getEmail().equals(authentication.getName())) {
            throw new UserNotAuthorizedException("Reserva pertence a outro usuario");
        }
    }
}
