package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.Laboratorio;
import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioRequestDTO;
import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioListDTO;
import com.seneca_brito.lab_manager.shared.DTOs.laboratorioDTOs.LaboratorioResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LaboratorioMapper {

    LaboratorioResponseDTO toDto(Laboratorio laboratorio);

    LaboratorioListDTO toListDto(Laboratorio laboratorio);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    @Mapping(target = "reclamacoes", ignore = true)
    Laboratorio toModel(LaboratorioRequestDTO laboratorioRequestDTO);
}
