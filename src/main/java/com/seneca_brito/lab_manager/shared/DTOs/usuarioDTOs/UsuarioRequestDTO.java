package com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs;

import jakarta.validation.constraints.*;

public record UsuarioRequestDTO(


        @NotBlank(message = "Campo obrigatorio")
        @Size(min = 3, max = 50, message = "nome deve ter entre 3 e 50 caracteres")
        String nome,

        @NotBlank(message = "Campo obrigatorio")
        @Email(message = "Insira um email válido")
        @Size(min = 5, max = 100, message = "email deve ter entre 5 e 100 caracteres")
        String email,

        @NotBlank(message = "Campo obrigatorio")
        @Size(max = 60, message = "senha deve ter no máximo 60 caracteres")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{8,}$",
                message = "A senha deve conter no mínimo 8 caracteres, 1 número e 1 caractere especial."
        )
        String senha,

        @NotBlank(message = "Campo obrigatorio")
        @Size(min = 5, max = 50, message = "curso deve ter entre 5 e 50 caracteres")
        String curso,

        @NotBlank(message = "Campo obrigatorio")
        @Size(max = 255, message = "matricula deve ter no maximo 255 caracteres")
        String matricula
) {
}
