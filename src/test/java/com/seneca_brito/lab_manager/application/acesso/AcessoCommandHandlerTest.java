package com.seneca_brito.lab_manager.application.acesso;

import com.seneca_brito.lab_manager.application.Commands.acesso.AcessoCommandHandler;
import com.seneca_brito.lab_manager.application.services.ReservaSettings;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.domain.RegistroAcesso;
import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.RegistroAcessoRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.ENUM.StatusAcesso;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcessoCommandHandlerTest {

    @Mock ReservaRepository reservaRepository;
    @Mock RegistroAcessoRepository acessoRepository;

    private ReservaSettings settings;
    private Reserva approved;
    private Authentication owner;

    @BeforeEach
    void setUp() {
        settings = new ReservaSettings("America/Sao_Paulo", LocalTime.of(7, 30),
                LocalTime.of(18, 0), 30, 72);
        Usuario user = Usuario.builder().id(UUID.randomUUID()).nome("Owner")
                .email("owner@test.local").senha("hash").curso("Computacao")
                .matricula("OWNER").build();
        Laboratorio laboratory = new Laboratorio();
        laboratory.setId(UUID.randomUUID());
        approved = new Reserva();
        approved.setId(UUID.randomUUID());
        approved.setUsuario(user);
        approved.setLaboratorio(laboratory);
        approved.setStatus(StatusReserva.APROVADA);
        approved.setDataReserva(LocalDate.of(2026, 8, 17));
        approved.setHorarioInicio(LocalTime.of(10, 0));
        approved.setHorarioFim(LocalTime.of(11, 0));
        owner = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                List.of(new SimpleGrantedAuthority("USUARIO")));
    }

    @Test
    void checkInAcceptsStartAndRejectsTimesOutsideSemiOpenWindow() {
        Instant start = Instant.parse("2026-08-17T13:00:00Z");
        when(reservaRepository.findByIdForUpdate(approved.getId())).thenReturn(Optional.of(approved));
        when(acessoRepository.existsByReservaId(approved.getId())).thenReturn(false);
        when(acessoRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RegistroAcesso access = handler(start).checkIn(approved.getId(), owner);
        assertEquals(start, access.getCheckIn());
        assertEquals(StatusAcesso.EM_ANDAMENTO, access.getStatus());

        for (Instant outside : List.of(Instant.parse("2026-08-17T12:59:59Z"),
                Instant.parse("2026-08-17T14:00:00Z"))) {
            reset(reservaRepository, acessoRepository);
            when(reservaRepository.findByIdForUpdate(approved.getId())).thenReturn(Optional.of(approved));
            when(acessoRepository.existsByReservaId(approved.getId())).thenReturn(false);
            assertThrows(RegraNegocioException.class,
                    () -> handler(outside).checkIn(approved.getId(), owner));
        }
    }

    @Test
    void administrationMayOperateReservationWithoutChangingItsOwner() {
        Instant now = Instant.parse("2026-08-17T13:30:00Z");
        Authentication admin = new UsernamePasswordAuthenticationToken("admin@test.local", null,
                List.of(new SimpleGrantedAuthority("ADMINISTRACAO")));
        when(reservaRepository.findByIdForUpdate(approved.getId())).thenReturn(Optional.of(approved));
        when(acessoRepository.existsByReservaId(approved.getId())).thenReturn(false);
        when(acessoRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RegistroAcesso access = handler(now).checkIn(approved.getId(), admin);
        assertEquals("owner@test.local", access.getReserva().getUsuario().getEmail());
    }

    private AcessoCommandHandler handler(Instant now) {
        return new AcessoCommandHandler(reservaRepository, acessoRepository, settings,
                Clock.fixed(now, ZoneId.of("America/Sao_Paulo")));
    }
}
