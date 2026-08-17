package com.seneca_brito.lab_manager.application.Commands.usuario.create;

import com.seneca_brito.lab_manager.application.Commands.autenticacao.cadastro.CadastroHandler;
import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateUsuarioHandler {

    private final CadastroHandler cadastroHandler;


    public Usuario create(UsuarioRequestDTO dto) {
        return cadastroHandler.create(dto);
    }
}
