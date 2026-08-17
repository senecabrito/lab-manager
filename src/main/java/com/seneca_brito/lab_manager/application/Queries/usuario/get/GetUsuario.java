package com.seneca_brito.lab_manager.application.Queries.usuario.get;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioMinDTO;
import com.seneca_brito.lab_manager.shared.mappers.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class GetUsuario {

    private final GetUsuarioHandler usuarioHandler;
    private final UsuarioMapper  usuarioMapper;

    @GetMapping
    public ResponseEntity<PagedModel<UsuarioMinDTO>> getAll(@ParameterObject Pageable pageRequest) {
        Page<Usuario> page = usuarioHandler.findAll(pageRequest);

        List<UsuarioMinDTO> usuariosDto = page.getContent().stream().map(usuarioMapper::toMinDto).toList();

        Page<UsuarioMinDTO> pageDto = new PageImpl<>(usuariosDto, page.getPageable(), page.getTotalElements());

        PagedModel<UsuarioMinDTO> pagedModel = new PagedModel<>(pageDto);

        return ResponseEntity.ok().body(pagedModel);
    }

}
