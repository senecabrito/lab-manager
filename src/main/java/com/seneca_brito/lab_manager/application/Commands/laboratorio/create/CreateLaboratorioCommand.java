package com.seneca_brito.lab_manager.application.Commands.laboratorio.create;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioRequestDTO;
import com.seneca_brito.lab_manager.shared.mappers.LaboratorioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/laboratorios")
@RequiredArgsConstructor
public class CreateLaboratorioCommand {

    private final CreateLaboratorioHandler laboratorioHandler;
    private final LaboratorioMapper laboratorioMapper;

    @PostMapping
    public ResponseEntity<Void> createLaboratorio(@RequestBody LaboratorioRequestDTO laboratorioDTO) {
        Laboratorio laboratorio = laboratorioMapper.toModel(laboratorioDTO);

        Laboratorio response = laboratorioHandler.create(laboratorio);
        String id = response.getId().toString();

        return ResponseEntity.created(URI.create(id)).build();
    }
}
