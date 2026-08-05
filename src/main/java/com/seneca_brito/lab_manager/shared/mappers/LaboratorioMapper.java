package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioRequestDTO;
import org.mapstruct.Mapper;

@Mapper
public interface LaboratorioMapper {

    LaboratorioRequestDTO toDto(Laboratorio laboratorio);

    Laboratorio toModel(LaboratorioRequestDTO laboratorioRequestDTO);
}
