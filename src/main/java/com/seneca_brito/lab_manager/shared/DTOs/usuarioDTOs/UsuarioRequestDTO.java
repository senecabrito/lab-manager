package com.seneca_brito.lab_manager.shared.DTOs.usuarioDTOs;

import jakarta.validation.constraints.*;

public record UsuarioRequestDTO(


        @NotBlank(message = "Campo obrigatorio")
        @Max(value = 50, message = "nome muito longo, crie uma com menos caracteres")
        @Min(value = 3, message = "nome muito longo, crie uma com menos caracteres")
        String nome,

        @NotBlank(message = "Campo obrigatorio")
        @Email(message = "Insira um email válido")
        @Max(value = 100, message = "email muito longo, crie uma com menos caracteres")
        @Min(value = 5, message = "email muito curto, crie uma com menos caracteres")
        String email,

        @NotBlank(message = "Campo obrigatorio")
        @Max(value = 60, message = "senha muito longa, crie uma com menos caracteres")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[@$!%*?&.#_-])[A-Za-z\\d@$!%*?&.#_-]{8,}$",
                message = "A senha deve conter no mínimo 8 caracteres, 1 número e 1 caractere especial."
        )
        String senha,

        @NotBlank
        @Max(value = 50, message = "nome do curso muito longo, crie uma com menos caracteres")
        @Min(value = 5, message = "nome do curso muito longo, crie uma com menos caracteres")
        String curso
) {
}
