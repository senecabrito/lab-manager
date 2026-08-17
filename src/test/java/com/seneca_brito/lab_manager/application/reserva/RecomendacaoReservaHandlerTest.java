package com.seneca_brito.lab_manager.application.reserva;

import com.seneca_brito.lab_manager.application.Queries.laboratorio.get.GetLaboratorioHandler;
import com.seneca_brito.lab_manager.application.Queries.reserva.recomendacao.RecomendacaoReservaHandler;
import com.seneca_brito.lab_manager.application.services.ReservaPolicy;
import com.seneca_brito.lab_manager.application.services.ReservaSettings;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs.RecomendacaoOpcaoDTO;
import com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs.RecomendacaoReservaRequestDTO;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecomendacaoReservaHandlerTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final Instant NOW = Instant.parse("2026-08-10T13:00:00Z");

    @Mock GetLaboratorioHandler laboratorioHandler;
    @Mock LaboratorioRepository laboratorioRepository;
    @Mock ReservaRepository reservaRepository;

    private RecomendacaoReservaHandler handler;

    @BeforeEach
    void setUp() {
        ReservaSettings settings = new ReservaSettings("America/Sao_Paulo",
                LocalTime.of(7, 30), LocalTime.of(18, 0), 30, 72);
        ReservaPolicy policy = new ReservaPolicy(settings, Clock.fixed(NOW, ZONE));
        handler = new RecomendacaoReservaHandler(laboratorioHandler, laboratorioRepository,
                reservaRepository, policy);
    }

    @Test
    void returnsCompatibleOptionsInDeterministicOrderWithoutPersonalData() {
        Laboratorio larger = laboratory("Beta", 40);
        Laboratorio sufficient = laboratory("Alfa", 30);
        when(laboratorioHandler.find(eq(25), eq("Bloco A"), eq(Set.of("PROJETOR")), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(larger, sufficient)));
        when(reservaRepository.existsConflito(any(), any(), any(), any(), anyCollection(), isNull()))
                .thenReturn(false);

        var response = handler.recommend(request(LocalDate.of(2026, 8, 20),
                LocalTime.of(10, 0), 60, 25, Set.of("PROJETOR"), "Bloco A", null));

        assertFalse(response.recomendacoes().isEmpty());
        RecomendacaoOpcaoDTO first = response.recomendacoes().getFirst();
        assertEquals(sufficient.getId(), first.laboratorioId());
        assertEquals(LocalTime.of(10, 0), first.inicio());
        assertEquals("America/Sao_Paulo", response.timezone());
        assertTrue(Arrays.stream(RecomendacaoOpcaoDTO.class.getRecordComponents())
                .map(component -> component.getName().toLowerCase())
                .noneMatch(name -> name.contains("usuario") || name.contains("email")
                        || name.contains("reserva")));
    }

    @Test
    void pendingAndApprovedBlockWhileRejectedAndCancelledDoNot() {
        Laboratorio laboratorio = laboratory("Lab", 30);
        when(laboratorioHandler.find(anyInt(), isNull(), anySet(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(laboratorio)));

        for (StatusReserva existingStatus : StatusReserva.values()) {
            reset(reservaRepository);
            when(reservaRepository.existsConflito(any(), any(), any(), any(), anyCollection(), isNull()))
                    .thenAnswer(invocation -> {
                        LocalTime inicio = invocation.getArgument(2);
                        @SuppressWarnings("unchecked")
                        Collection<StatusReserva> blocking = invocation.getArgument(4);
                        return inicio.equals(LocalTime.of(7, 30)) && blocking.contains(existingStatus);
                    });

            var response = handler.recommend(request(LocalDate.of(2026, 8, 20), null,
                    30, 10, Set.of(), null, null));
            LocalTime firstStart = response.recomendacoes().stream()
                    .map(RecomendacaoOpcaoDTO::inicio).min(LocalTime::compareTo).orElseThrow();
            if (existingStatus == StatusReserva.PENDENTE || existingStatus == StatusReserva.APROVADA) {
                assertEquals(LocalTime.of(8, 0), firstStart, existingStatus.name());
            } else {
                assertEquals(LocalTime.of(7, 30), firstStart, existingStatus.name());
            }
        }
    }

    @Test
    void reusesAdvanceOperatingHoursAndSlotRules() {
        Laboratorio laboratorio = laboratory("Lab", 30);
        when(laboratorioHandler.find(anyInt(), isNull(), anySet(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(laboratorio)));
        when(reservaRepository.existsConflito(any(), any(), any(), any(), anyCollection(), isNull()))
                .thenReturn(false);

        var exactlySeventyTwoHours = handler.recommend(request(LocalDate.of(2026, 8, 13), null,
                30, 10, Set.of(), null, null));
        assertEquals(LocalTime.of(10, 0), exactlySeventyTwoHours.recomendacoes().getFirst().inicio());
        assertTrue(exactlySeventyTwoHours.recomendacoes().stream()
                .allMatch(item -> !item.inicio().isBefore(LocalTime.of(7, 30))
                        && !item.fim().isAfter(LocalTime.of(18, 0))
                        && item.inicio().getMinute() % 30 == 0
                        && item.fim().getMinute() % 30 == 0));

        var tooSoon = handler.recommend(request(LocalDate.of(2026, 8, 12), null,
                30, 10, Set.of(), null, null));
        assertTrue(tooSoon.recomendacoes().isEmpty());
        assertThrows(RegraNegocioException.class, () -> handler.recommend(request(
                LocalDate.of(2026, 8, 20), null, 45, 10, Set.of(), null, null)));
    }

    @Test
    void rejectsMissingExplicitLaboratory() {
        UUID missing = UUID.randomUUID();
        when(laboratorioRepository.existsById(missing)).thenReturn(false);
        assertThrows(RecursoNaoEncontradoException.class, () -> handler.recommend(request(
                LocalDate.of(2026, 8, 20), null, 30, 10, Set.of(), null, missing)));
        verifyNoInteractions(laboratorioHandler, reservaRepository);
    }

    private RecomendacaoReservaRequestDTO request(LocalDate date, LocalTime preferred, int duration,
                                                   int students, Set<String> resources, String location,
                                                   UUID laboratoryId) {
        return new RecomendacaoReservaRequestDTO(date, preferred, duration, students,
                resources, location, laboratoryId);
    }

    private Laboratorio laboratory(String name, int capacity) {
        Laboratorio laboratory = new Laboratorio();
        laboratory.setId(UUID.randomUUID());
        laboratory.setNome(name);
        laboratory.setCapacidade(capacity);
        laboratory.setLocalizacao("Bloco A");
        laboratory.setRecursos(Set.of("PROJETOR"));
        return laboratory;
    }
}
