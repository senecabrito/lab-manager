package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReclamacaoMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "laboratorioId", source = "laboratorio.id")
    ReclamacaoResponseDTO toDto(Reclamacao reclamacao);
}
