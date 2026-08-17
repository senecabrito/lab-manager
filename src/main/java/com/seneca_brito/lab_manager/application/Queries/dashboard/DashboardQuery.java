package com.seneca_brito.lab_manager.application.Queries.dashboard;

import com.seneca_brito.lab_manager.shared.DTOs.dashboardDTOs.DashboardResumoDTO;
import com.seneca_brito.lab_manager.shared.DTOs.dashboardDTOs.HistoricoDTO;
import com.seneca_brito.lab_manager.shared.DTOs.dashboardDTOs.UtilizacaoLaboratorioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardQuery {

    private final DashboardHandler handler;

    @GetMapping("/api/v1/dashboard")
    public ResponseEntity<DashboardResumoDTO> summary() {
        return ResponseEntity.ok(handler.summary());
    }

    @GetMapping("/api/v1/relatorios/historico")
    public ResponseEntity<HistoricoDTO> history(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {
        return ResponseEntity.ok(handler.history(dataInicial, dataFinal));
    }

    @GetMapping("/api/v1/relatorios/utilizacao")
    public ResponseEntity<List<UtilizacaoLaboratorioDTO>> utilization(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {
        return ResponseEntity.ok(handler.utilization(dataInicial, dataFinal, false));
    }

    @GetMapping("/api/v1/relatorios/ranking-laboratorios")
    public ResponseEntity<List<UtilizacaoLaboratorioDTO>> ranking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal) {
        return ResponseEntity.ok(handler.utilization(dataInicial, dataFinal, true));
    }
}
