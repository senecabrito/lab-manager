package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.Reclamacao;
import com.seneca_brito.lab_manager.shared.DTOs.reclamacaoDTOs.ReclamacaoRequestDTO;
import org.mapstruct.Mapper;

@Mapper
public interface ReclamacaoMapper {

    ReclamacaoRequestDTO toDto(Reclamacao reclamacao);

    Reclamacao toModel(ReclamacaoRequestDTO reclamacaoRequestDTO);
}
