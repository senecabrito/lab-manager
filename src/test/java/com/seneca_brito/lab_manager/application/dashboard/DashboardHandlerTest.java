package com.seneca_brito.lab_manager.application.dashboard;

import com.seneca_brito.lab_manager.application.Queries.dashboard.DashboardHandler;
import com.seneca_brito.lab_manager.application.services.*;
import com.seneca_brito.lab_manager.infrastructure.repositories.*;
import com.seneca_brito.lab_manager.shared.ENUM.*;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardHandlerTest {

    @Mock LaboratorioRepository laboratorioRepository;
    @Mock ReservaRepository reservaRepository;
    @Mock ReclamacaoRepository reclamacaoRepository;
    private DashboardHandler handler;

    @BeforeEach
    void setUp() {
        ReservaSettings settings = new ReservaSettings("America/Sao_Paulo",
                LocalTime.of(7, 30), LocalTime.of(18, 0), 30, 72);
        ReservaPolicy policy = new ReservaPolicy(settings, Clock.system(settings.zoneId()));
        handler = new DashboardHandler(laboratorioRepository, reservaRepository,
                reclamacaoRepository, policy);
    }

    @Test
    void summaryUsesRepositoryAggregations() {
        when(laboratorioRepository.count()).thenReturn(3L);
        when(reservaRepository.count()).thenReturn(8L);
        when(reclamacaoRepository.count()).thenReturn(5L);
        when(reclamacaoRepository.countByStatusIn(anyCollection())).thenReturn(2L);
        ReservaStatusQuantidadeProjection reservaStatus = reservaStatus(StatusReserva.APROVADA, 4);
        ReclamacaoCategoriaQuantidadeProjection categoria =
                categoria(CategoriaProblema.EQUIPAMENTO, 3);
        when(reservaRepository.countGroupedByStatus()).thenReturn(List.of(reservaStatus));
        when(reclamacaoRepository.countGroupedByCategoria()).thenReturn(List.of(categoria));
        var result = handler.summary();
        assertAll(
                () -> assertEquals(3, result.laboratorios()),
                () -> assertEquals(8, result.reservas()),
                () -> assertEquals(5, result.reclamacoes()),
                () -> assertEquals(2, result.problemasPendentes()),
                () -> assertEquals("APROVADA", result.reservasPorStatus().getFirst().chave()));
    }

    @Test
    void invalidHistoricalPeriodIsRejectedWithoutQueries() {
        assertThrows(RegraNegocioException.class, () -> handler.history(
                LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 1)));
        verifyNoInteractions(reservaRepository, reclamacaoRepository, laboratorioRepository);
    }

    @Test
    void historyRespectsInclusiveDateRange() {
        LocalDate inicio = LocalDate.of(2026, 8, 1);
        LocalDate fim = LocalDate.of(2026, 8, 2);
        when(reservaRepository.countGroupedByStatusAndPeriod(inicio, fim)).thenReturn(List.of());
        when(reclamacaoRepository.countGroupedByStatusAndPeriod(
                inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay())).thenReturn(List.of());
        var result = handler.history(inicio, fim);
        assertEquals(inicio, result.dataInicial());
        assertEquals(fim, result.dataFinal());
    }

    @Test
    void utilizationIncludesLaboratoryWithoutReservations() {
        UUID id = UUID.randomUUID();
        LaboratorioBasicoProjection lab = lab(id, "Lab vazio");
        when(laboratorioRepository.findAllBasic()).thenReturn(List.of(lab));
        when(reservaRepository.sumOccupiedMinutes(any(), any(), anyCollection())).thenReturn(List.of());
        var result = handler.utilization(LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1), false).getFirst();
        assertEquals(0, result.minutosOcupados());
        assertEquals(630, result.minutosDisponiveis());
        assertEquals(0, result.percentualUtilizacao().signum());
        verify(reservaRepository).sumOccupiedMinutes(LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1), Set.of(StatusReserva.APROVADA.name()));
    }

    @Test
    void rankingUsesOccupiedMinutesThenDeterministicName() {
        UUID idA = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID idB = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID idC = UUID.fromString("00000000-0000-0000-0000-000000000003");
        List<LaboratorioBasicoProjection> labs = List.of(
                lab(idC, "Zulu"), lab(idB, "Beta"), lab(idA, "Alpha"));
        List<LaboratorioOcupacaoProjection> occupations = List.of(
                occupation(idA, 60), occupation(idB, 120), occupation(idC, 120));
        when(laboratorioRepository.findAllBasic()).thenReturn(labs);
        when(reservaRepository.sumOccupiedMinutes(any(), any(), anyCollection())).thenReturn(occupations);
        var ranking = handler.utilization(LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1), true);
        assertEquals(List.of(idB, idC, idA),
                ranking.stream().map(item -> item.laboratorioId()).toList());
    }

    private ReservaStatusQuantidadeProjection reservaStatus(StatusReserva status, long count) {
        ReservaStatusQuantidadeProjection projection = mock(ReservaStatusQuantidadeProjection.class);
        when(projection.getStatus()).thenReturn(status);
        when(projection.getQuantidade()).thenReturn(count);
        return projection;
    }

    private ReclamacaoCategoriaQuantidadeProjection categoria(CategoriaProblema categoria, long count) {
        ReclamacaoCategoriaQuantidadeProjection projection = mock(ReclamacaoCategoriaQuantidadeProjection.class);
        when(projection.getCategoria()).thenReturn(categoria);
        when(projection.getQuantidade()).thenReturn(count);
        return projection;
    }

    private LaboratorioBasicoProjection lab(UUID id, String nome) {
        LaboratorioBasicoProjection projection = mock(LaboratorioBasicoProjection.class);
        when(projection.getId()).thenReturn(id);
        when(projection.getNome()).thenReturn(nome);
        return projection;
    }

    private LaboratorioOcupacaoProjection occupation(UUID id, long minutes) {
        LaboratorioOcupacaoProjection projection = mock(LaboratorioOcupacaoProjection.class);
        when(projection.getLaboratorioHex()).thenReturn(id.toString().replace("-", "").toUpperCase());
        when(projection.getMinutosOcupados()).thenReturn(minutes);
        return projection;
    }
}
