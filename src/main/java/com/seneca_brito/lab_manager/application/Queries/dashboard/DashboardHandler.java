package com.seneca_brito.lab_manager.application.Queries.dashboard;

import com.seneca_brito.lab_manager.application.services.ReservaPolicy;
import com.seneca_brito.lab_manager.infrastructure.repositories.*;
import com.seneca_brito.lab_manager.shared.DTOs.dashboardDTOs.*;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReclamacao;
import com.seneca_brito.lab_manager.shared.ENUM.StatusReserva;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardHandler {

    private final LaboratorioRepository laboratorioRepository;
    private final ReservaRepository reservaRepository;
    private final ReclamacaoRepository reclamacaoRepository;
    private final ReservaPolicy reservaPolicy;

    @Transactional(readOnly = true)
    public DashboardResumoDTO summary() {
        List<IndicadorDTO> reservas = reservaRepository.countGroupedByStatus().stream()
                .map(item -> new IndicadorDTO(item.getStatus().name(), item.getQuantidade()))
                .toList();
        List<IndicadorDTO> categorias = reclamacaoRepository.countGroupedByCategoria().stream()
                .map(item -> new IndicadorDTO(item.getCategoria().name(), item.getQuantidade()))
                .toList();
        long problemasPendentes = reclamacaoRepository.countByStatusIn(
                Set.of(StatusReclamacao.PENDENTE, StatusReclamacao.EM_ANALISE));
        return new DashboardResumoDTO(laboratorioRepository.count(), reservaRepository.count(),
                reservas, reclamacaoRepository.count(), problemasPendentes, categorias);
    }

    @Transactional(readOnly = true)
    public HistoricoDTO history(LocalDate inicio, LocalDate fim) {
        validatePeriod(inicio, fim);
        List<IndicadorDTO> reservas = reservaRepository.countGroupedByStatusAndPeriod(inicio, fim)
                .stream().map(item -> new IndicadorDTO(item.getStatus().name(), item.getQuantidade()))
                .toList();
        List<IndicadorDTO> reclamacoes = reclamacaoRepository.countGroupedByStatusAndPeriod(
                        inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay())
                .stream().map(item -> new IndicadorDTO(item.getStatus().name(), item.getQuantidade()))
                .toList();
        return new HistoricoDTO(inicio, fim, reservas, reclamacoes);
    }

    @Transactional(readOnly = true)
    public List<UtilizacaoLaboratorioDTO> utilization(LocalDate inicio, LocalDate fim,
                                                       boolean ranking) {
        validatePeriod(inicio, fim);
        Set<String> utilizationStatuses = StatusReserva.estadosQueContamComoUtilizacao().stream()
                .map(Enum::name).collect(Collectors.toSet());
        Map<UUID, Long> occupied = reservaRepository.sumOccupiedMinutes(
                        inicio, fim, utilizationStatuses)
                .stream().collect(Collectors.toMap(
                        item -> uuidFromHex(item.getLaboratorioHex()),
                        item -> item.getMinutosOcupados().longValue()));
        long days = ChronoUnit.DAYS.between(inicio, fim) + 1;
        long dailyMinutes = Duration.between(reservaPolicy.settings().openingTime(),
                reservaPolicy.settings().closingTime()).toMinutes();
        long available = Math.multiplyExact(days, dailyMinutes);
        List<UtilizacaoLaboratorioDTO> result = laboratorioRepository.findAllBasic().stream()
                .map(lab -> utilizationDto(lab, occupied.getOrDefault(lab.getId(), 0L), available))
                .collect(Collectors.toCollection(ArrayList::new));
        Comparator<UtilizacaoLaboratorioDTO> order = ranking
                ? Comparator.comparingLong(UtilizacaoLaboratorioDTO::minutosOcupados).reversed()
                    .thenComparing(UtilizacaoLaboratorioDTO::laboratorio, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(UtilizacaoLaboratorioDTO::laboratorioId)
                : Comparator.comparing(UtilizacaoLaboratorioDTO::laboratorio,
                    String.CASE_INSENSITIVE_ORDER).thenComparing(UtilizacaoLaboratorioDTO::laboratorioId);
        result.sort(order);
        return List.copyOf(result);
    }

    private UtilizacaoLaboratorioDTO utilizationDto(LaboratorioBasicoProjection lab,
                                                      long occupied, long available) {
        BigDecimal percentage = available == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(occupied).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(available), 2, RoundingMode.HALF_UP);
        return new UtilizacaoLaboratorioDTO(lab.getId(), lab.getNome(), occupied, available, percentage);
    }

    private void validatePeriod(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new RegraNegocioException("Intervalo de datas invalido");
        }
    }

    private UUID uuidFromHex(String hex) {
        if (hex == null || hex.length() != 32) {
            throw new IllegalStateException("Identificador agregado de laboratorio invalido");
        }
        String value = hex.substring(0, 8) + "-" + hex.substring(8, 12) + "-"
                + hex.substring(12, 16) + "-" + hex.substring(16, 20) + "-" + hex.substring(20);
        return UUID.fromString(value);
    }
}
