package com.seneca_brito.lab_manager.application.Commands.autenticacao.cadastro;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.RolesRepository;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.infrastructure.security.RolesEntity;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioRequestDTO;
import com.seneca_brito.lab_manager.shared.ENUM.RoleTypeEnum;
import com.seneca_brito.lab_manager.shared.ENUM.TipoDeUsuarios;
import com.seneca_brito.lab_manager.shared.exceptions.RegistroDuplicadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CadastroHandler {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final RolesRepository rolesRepository;

    public Usuario create(UsuarioRequestDTO dto) {
        if(usuarioRepository.existsByEmail(dto.email())){
            throw new RegistroDuplicadoException("email já estão em uso");
        }

        RolesEntity roles = rolesRepository.findByNome(RoleTypeEnum.USUARIO.name())
                .orElseGet(()-> rolesRepository.save(RolesEntity.builder().nome(RoleTypeEnum.USUARIO.name())
                        .build()));


        return usuarioRepository.save(Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .curso(dto.curso())
                .tipoDeUsuarios(TipoDeUsuarios.PROF)
                .roles(Set.of(roles))
                .senha(encoder.encode(dto.senha()))
                .build());
    }
}
