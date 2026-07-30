package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.UsuarioRequestDTO;
import org.mapstruct.Mapper;

@Mapper
public interface UsuarioMapper {

    UsuarioRequestDTO toDto(Usuario usuario);

    Usuario toModel(UsuarioRequestDTO usuarioRequestDTO);
}
