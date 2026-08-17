package com.seneca_brito.lab_manager.shared.DTOs.loginDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginDTO (
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
        String senha
){
}
