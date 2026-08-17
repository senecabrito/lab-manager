package com.seneca_brito.lab_manager.application.calendario;

import com.seneca_brito.lab_manager.application.Queries.calendario.GetCalendarioHandler;
import com.seneca_brito.lab_manager.application.services.ReservaPolicy;
import com.seneca_brito.lab_manager.application.services.ReservaSettings;
import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCalendarioHandlerTest {

    @Mock LaboratorioRepository laboratorioRepository;
    @Mock ReservaRepository reservaRepository;
    private GetCalendarioHandler handler;
    private UUID laboratorioId;
    private LocalDate data;

    @BeforeEach
    void setUp() {
        ReservaSettings settings = new ReservaSettings("America/Sao_Paulo",
                LocalTime.of(7, 30), LocalTime.of(18, 0), 30, 72);
        ReservaPolicy policy = new ReservaPolicy(settings,
                Clock.fixed(Instant.parse("2026-08-17T13:00:00Z"), settings.zoneId()));
        handler = new GetCalendarioHandler(laboratorioRepository, reservaRepository, policy);
        laboratorioId = UUID.randomUUID();
        data = LocalDate.of(2026, 8, 21);
        when(laboratorioRepository.existsById(laboratorioId)).thenReturn(true);
    }

    @Test
    void emptyCalendarReturnsAllOperatingSlotsFree() {
        when(reservaRepository.findAgenda(eq(laboratorioId), eq(data), anyCollection()))
                .thenReturn(List.of());
        var result = handler.get(laboratorioId, data);
        assertEquals(21, result.slots().size());
        assertTrue(result.slots().stream().noneMatch(slot -> slot.ocupado()));
        assertEquals(LocalTime.of(7, 30), result.slots().getFirst().inicio());
        assertEquals(LocalTime.of(8, 0), result.slots().getFirst().fim());
        assertEquals(LocalTime.of(17, 30), result.slots().getLast().inicio());
        assertEquals(LocalTime.of(18, 0), result.slots().getLast().fim());
        assertTrue(result.slots().stream().allMatch(slot ->
                Duration.between(slot.inicio(), slot.fim()).toMinutes() == 30));
        assertTrue(result.slots().stream().noneMatch(slot ->
                slot.inicio().isBefore(LocalTime.of(7, 30))
                        || slot.fim().isAfter(LocalTime.of(18, 0))));
    }

    @Test
    void occupiedReservationMarksOnlyOverlappingSlots() {
        when(reservaRepository.findAgenda(eq(laboratorioId), eq(data), anyCollection()))
                .thenReturn(List.of(reserva(LocalTime.of(10, 0), LocalTime.NOON)));
        var slots = handler.get(laboratorioId, data).slots();
        assertEquals(4, slots.stream().filter(slot -> slot.ocupado()).count());
        assertFalse(slots.get(4).ocupado());
        assertTrue(slots.get(5).ocupado());
        assertTrue(slots.get(8).ocupado());
        assertFalse(slots.get(9).ocupado());
    }

    @Test
    void adjacentReservationsDoNotCreateFalseOverlap() {
        when(reservaRepository.findAgenda(eq(laboratorioId), eq(data), anyCollection()))
                .thenReturn(List.of(reserva(LocalTime.of(10, 0), LocalTime.of(11, 0)),
                        reserva(LocalTime.of(11, 0), LocalTime.NOON)));
        var slots = handler.get(laboratorioId, data).slots();
        assertEquals(4, slots.stream().filter(slot -> slot.ocupado()).count());
    }

    @Test
    void responseUsesExplicitTimezone() {
        when(reservaRepository.findAgenda(any(), any(), anyCollection())).thenReturn(List.of());
        assertEquals("America/Sao_Paulo", handler.get(laboratorioId, data).timezone());
        verify(reservaRepository).findAgenda(laboratorioId, data,
                Set.of(StatusReserva.PENDENTE, StatusReserva.APROVADA));
    }

    @Test
    void missingLaboratoryReturnsNotFound() {
        when(laboratorioRepository.existsById(laboratorioId)).thenReturn(false);
        assertThrows(RecursoNaoEncontradoException.class, () -> handler.get(laboratorioId, data));
    }

    private Reserva reserva(LocalTime inicio, LocalTime fim) {
        Reserva reserva = new Reserva();
        reserva.setHorarioInicio(inicio);
        reserva.setHorarioFim(fim);
        return reserva;
    }
}
