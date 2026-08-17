package com.seneca_brito.lab_manager.application.services;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class ReservaPolicyTest {

    private ReservaPolicy policy;
    private Laboratorio laboratorio;

    @BeforeEach
    void setUp() {
        ReservaSettings settings = new ReservaSettings("America/Sao_Paulo",
                LocalTime.of(7, 30), LocalTime.of(18, 0), 30, 72);
        Clock clock = Clock.fixed(
                ZonedDateTime.of(2026, 8, 17, 10, 0, 0, 0, settings.zoneId()).toInstant(),
                settings.zoneId());
        policy = new ReservaPolicy(settings, clock);
        laboratorio = new Laboratorio();
        laboratorio.setCapacidade(40);
    }

    @Test
    void acceptsExactlySeventyTwoHours() {
        assertDoesNotThrow(() -> policy.validate(laboratorio, LocalDate.of(2026, 8, 20),
                LocalTime.of(10, 0), LocalTime.of(11, 0), 40));
    }

    @Test
    void rejectsLessThanSeventyTwoRealHours() {
        assertThrows(RegraNegocioException.class, () -> policy.validate(laboratorio,
                LocalDate.of(2026, 8, 20), LocalTime.of(9, 0), LocalTime.of(10, 0), 20));
    }

    @Test
    void rejectsZeroAndNegativeQuantity() {
        assertAll(
                () -> assertThrows(RegraNegocioException.class, () -> validAt(0)),
                () -> assertThrows(RegraNegocioException.class, () -> validAt(-1)));
    }

    @Test
    void rejectsQuantityAboveCapacity() {
        assertThrows(RegraNegocioException.class, () -> validAt(41));
    }

    @Test
    void rejectsZeroOrInvertedInterval() {
        assertAll(
                () -> assertThrows(RegraNegocioException.class, () -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.NOON, LocalTime.NOON, 10)),
                () -> assertThrows(RegraNegocioException.class, () -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.of(13, 0), LocalTime.NOON, 10)));
    }

    @Test
    void rejectsPeriodOutsideOpeningHours() {
        assertAll(
                () -> assertThrows(RegraNegocioException.class, () -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.of(7, 0), LocalTime.of(8, 0), 10)),
                () -> assertThrows(RegraNegocioException.class, () -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.of(17, 30), LocalTime.of(18, 30), 10)));
    }

    @Test
    void rejectsTimesOutsideConfiguredSlotGranularity() {
        assertAll(
                () -> assertThrows(RegraNegocioException.class, () -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.of(8, 15), LocalTime.of(9, 0), 10)),
                () -> assertThrows(RegraNegocioException.class, () -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.of(8, 0), LocalTime.of(9, 45), 10)));
    }

    @Test
    void acceptsOpeningClosingHalfHourAndMultipleSlotBoundaries() {
        assertAll(
                () -> assertDoesNotThrow(() -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.of(7, 30), LocalTime.of(8, 0), 10)),
                () -> assertDoesNotThrow(() -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.of(8, 0), LocalTime.of(8, 30), 10)),
                () -> assertDoesNotThrow(() -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.of(8, 0), LocalTime.of(9, 30), 10)),
                () -> assertDoesNotThrow(() -> policy.validate(laboratorio,
                        LocalDate.of(2026, 8, 21), LocalTime.of(17, 0), LocalTime.of(18, 0), 10)));
    }

    @Test
    void exposesExplicitProjectTimezoneAndBlockingStates() {
        assertEquals("America/Sao_Paulo", policy.settings().zoneId().getId());
        assertEquals(2, policy.estadosQueBloqueiam().size());
        assertAll(
                () -> assertTrue(StatusReserva.PENDENTE.bloqueiaHorario()),
                () -> assertTrue(StatusReserva.APROVADA.bloqueiaHorario()),
                () -> assertFalse(StatusReserva.REJEITADA.bloqueiaHorario()),
                () -> assertFalse(StatusReserva.CANCELADA.bloqueiaHorario()),
                () -> assertFalse(StatusReserva.PENDENTE.contaComoUtilizacao()),
                () -> assertTrue(StatusReserva.APROVADA.contaComoUtilizacao()),
                () -> assertFalse(StatusReserva.REJEITADA.contaComoUtilizacao()),
                () -> assertFalse(StatusReserva.CANCELADA.contaComoUtilizacao()));
    }

    private void validAt(int quantidade) {
        policy.validate(laboratorio, LocalDate.of(2026, 8, 21),
                LocalTime.of(10, 0), LocalTime.of(11, 0), quantidade);
    }
}
