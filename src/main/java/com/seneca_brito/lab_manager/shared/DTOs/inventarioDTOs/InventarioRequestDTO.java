package com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InventarioRequestDTO(
        @NotBlank(message = "Campo obrigatorio")
        @Size(max = 100, message = "nome deve ter no maximo 100 caracteres") String nome,
        @NotNull(message = "Campo obrigatorio")
        @PositiveOrZero(message = "quantidade disponivel deve ser positiva ou zero")
        Integer quantidadeDisponivel,
        @NotNull(message = "Campo obrigatorio")
        @PositiveOrZero(message = "quantidade indisponivel deve ser positiva ou zero")
        Integer quantidadeIndisponivel,
        @NotNull(message = "Campo obrigatorio") UUID laboratorioId
) {
}
