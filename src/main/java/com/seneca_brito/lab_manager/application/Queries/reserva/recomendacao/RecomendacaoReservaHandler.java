package com.seneca_brito.lab_manager.application.Queries.reserva.recomendacao;

import com.seneca_brito.lab_manager.application.Queries.laboratorio.get.GetLaboratorioHandler;
import com.seneca_brito.lab_manager.application.services.ReservaPolicy;
import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.ReservaRepository;
import com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs.RecomendacaoOpcaoDTO;
import com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs.RecomendacaoReservaRequestDTO;
import com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs.RecomendacaoReservaResponseDTO;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecomendacaoReservaHandler {

    private final GetLaboratorioHandler getLaboratorioHandler;
    private final LaboratorioRepository laboratorioRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaPolicy reservaPolicy;

    @Transactional(readOnly = true)
    public RecomendacaoReservaResponseDTO recommend(RecomendacaoReservaRequestDTO dto) {
        int slotMinutes = reservaPolicy.settings().slotMinutes();
        if (dto.duracaoMinutos() % slotMinutes != 0) {
            throw new RegraNegocioException("Duracao deve respeitar a granularidade da agenda");
        }
        long operatingMinutes = Duration.between(reservaPolicy.settings().openingTime(),
                reservaPolicy.settings().closingTime()).toMinutes();
        if (dto.duracaoMinutos() > operatingMinutes) {
            throw new RegraNegocioException("Duracao excede o horario de funcionamento");
        }
        if (dto.laboratorioId() != null && !laboratorioRepository.existsById(dto.laboratorioId())) {
            throw new RecursoNaoEncontradoException("Laboratorio nao encontrado");
        }

        Sort sort = Sort.by("capacidade").ascending()
                .and(Sort.by("nome").ascending())
                .and(Sort.by("id").ascending());
        List<Laboratorio> laboratorios = getLaboratorioHandler.find(dto.quantidadeAlunos(),
                        dto.localizacao(), dto.recursos(), Pageable.unpaged(sort))
                .getContent().stream()
                .filter(lab -> dto.laboratorioId() == null || lab.getId().equals(dto.laboratorioId()))
                .toList();

        List<RecomendacaoOpcaoDTO> recomendacoes = new ArrayList<>();
        for (Laboratorio laboratorio : laboratorios) {
            LocalTime inicio = reservaPolicy.settings().openingTime();
            while (!inicio.plusMinutes(dto.duracaoMinutos())
                    .isAfter(reservaPolicy.settings().closingTime())) {
                LocalTime fim = inicio.plusMinutes(dto.duracaoMinutos());
                if (isValidAndAvailable(laboratorio, dto, inicio, fim)) {
                    recomendacoes.add(new RecomendacaoOpcaoDTO(laboratorio.getId(), laboratorio.getNome(),
                            laboratorio.getCapacidade(), laboratorio.getLocalizacao(),
                            laboratorio.getRecursos() == null ? java.util.Set.of()
                                    : java.util.Collections.unmodifiableSet(
                                            new java.util.TreeSet<>(laboratorio.getRecursos())),
                            dto.data(), inicio, fim, true));
                }
                inicio = inicio.plusMinutes(slotMinutes);
            }
        }

        LocalTime preferred = dto.horarioPreferencial() == null
                ? reservaPolicy.settings().openingTime() : dto.horarioPreferencial();
        Comparator<RecomendacaoOpcaoDTO> comparator = Comparator
                .comparing(RecomendacaoOpcaoDTO::capacidade)
                .thenComparingLong(item -> Math.abs(Duration.between(preferred, item.inicio()).toMinutes()))
                .thenComparing(RecomendacaoOpcaoDTO::laboratorio, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RecomendacaoOpcaoDTO::laboratorioId)
                .thenComparing(RecomendacaoOpcaoDTO::inicio);
        recomendacoes.sort(comparator);

        return new RecomendacaoReservaResponseDTO(reservaPolicy.settings().zoneId().getId(),
                List.copyOf(recomendacoes));
    }

    private boolean isValidAndAvailable(Laboratorio laboratorio, RecomendacaoReservaRequestDTO dto,
                                        LocalTime inicio, LocalTime fim) {
        try {
            reservaPolicy.validate(laboratorio, dto.data(), inicio, fim, dto.quantidadeAlunos());
        } catch (RegraNegocioException exception) {
            return false;
        }
        return !reservaRepository.existsConflito(laboratorio.getId(), dto.data(), inicio, fim,
                reservaPolicy.estadosQueBloqueiam(), null);
    }
}
