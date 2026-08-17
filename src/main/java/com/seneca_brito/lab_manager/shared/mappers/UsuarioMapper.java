package com.seneca_brito.lab_manager.shared.mappers;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioMinDTO;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioRequestDTO;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsuarioMapper {

    UsuarioResponseDTO toDto(Usuario usuario);

    UsuarioMinDTO toMinDto(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoDeUsuarios", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    @Mapping(target = "reclamacoes", ignore = true)
    @Mapping(target = "roles", ignore = true)
    Usuario toModel(UsuarioRequestDTO usuarioRequestDTO);
}
