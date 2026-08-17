package com.seneca_brito.lab_manager.application.Commands.usuario.update;

import com.seneca_brito.lab_manager.domain.Usuario;
import com.seneca_brito.lab_manager.infrastructure.repositories.UsuarioRepository;
import com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs.UsuarioUpdateDTO;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.RegistroDuplicadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUsuarioHandler {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Usuario update(UUID id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado"));
        return applyAndSave(usuario, dto);
    }

    @Transactional
    public Usuario updateAuthenticated(String email, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario autenticado nao encontrado"));
        return applyAndSave(usuario, dto);
    }

    private Usuario applyAndSave(Usuario usuario, UsuarioUpdateDTO dto) {
        if (dto.email() != null && usuarioRepository.existsByEmailAndIdNot(dto.email(), usuario.getId())) {
            throw new RegistroDuplicadoException("Email ja esta em uso");
        }
        if (dto.matricula() != null
                && usuarioRepository.existsByMatriculaAndIdNot(dto.matricula(), usuario.getId())) {
            throw new RegistroDuplicadoException("Matricula ja esta em uso");
        }

        if (dto.nome() != null) {
            usuario.setNome(dto.nome());
        }
        if (dto.email() != null) {
            usuario.setEmail(dto.email());
        }
        if (dto.curso() != null) {
            usuario.setCurso(dto.curso());
        }
        if (dto.matricula() != null) {
            usuario.setMatricula(dto.matricula());
        }

        return usuarioRepository.saveAndFlush(usuario);
    }
}
