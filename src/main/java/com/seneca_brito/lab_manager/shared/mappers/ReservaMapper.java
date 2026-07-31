package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.Reserva;
import com.seneca_brito.lab_manager.shared.DTOs.reservaDTOs.ReservaRequestDTO;
import org.mapstruct.Mapper;

@Mapper
public interface ReservaMapper {

    ReservaRequestDTO toDto(Reserva reserva);

    Reserva toModel(ReservaRequestDTO reservaRequestDTO);
}
