package com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateDTO(
        @Size(min = 3, max = 50, message = "nome deve ter entre 3 e 50 caracteres")
        String nome,

        @Email(message = "Insira um email valido")
        @Size(min = 5, max = 100, message = "email deve ter entre 5 e 100 caracteres")
        String email,

        @Size(min = 5, max = 50, message = "curso deve ter entre 5 e 50 caracteres")
        String curso,

        @Pattern(regexp = ".*\\S.*", message = "matricula nao pode estar em branco")
        @Size(max = 255, message = "matricula deve ter no maximo 255 caracteres")
        String matricula
) {
}
