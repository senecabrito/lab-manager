package com.seneca_brito.lab_manager.application.Commands.laboratorio.create;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.infrastructure.repositories.LaboratorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CreateLaboratorioHandler {

    private final LaboratorioRepository laboratorioRepository;


    public Laboratorio create(Laboratorio laboratorio) {
        laboratorio.setLocalizacao(normalizeOptional(laboratorio.getLocalizacao()));
        laboratorio.setRecursos(normalizeResources(laboratorio.getRecursos()));
        return laboratorioRepository.saveAndFlush(laboratorio);
    }

    public static LinkedHashSet<String> normalizeResources(Iterable<String> recursos) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (recursos != null) {
            recursos.forEach(recurso -> normalized.add(recurso.trim().toUpperCase(Locale.ROOT)));
        }
        return normalized;
    }

    public static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
