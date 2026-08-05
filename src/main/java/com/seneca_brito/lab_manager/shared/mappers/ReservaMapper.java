package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaRequestDTO;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservaMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "laboratorioId", source = "laboratorio.id")
    ReservaResponseDTO toDto(Reserva reserva);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario.id", source = "usuarioId")
    @Mapping(target = "laboratorio.id", source = "laboratorioId")
    Reserva toModel(ReservaRequestDTO reservaRequestDTO);
}
