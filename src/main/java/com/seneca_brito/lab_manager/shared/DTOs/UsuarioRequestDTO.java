package com.seneca_brito.lab_manager.shared.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

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
        @Min(value = 5, message = "senha muito curta, crie uma com menos caracteres")
        String senha,

        @NotBlank
        @Max(value = 50, message = "nome do curso muito longo, crie uma com menos caracteres")
        @Min(value = 5, message = "nome do curso muito longo, crie uma com menos caracteres")
        String curso
) {
}
