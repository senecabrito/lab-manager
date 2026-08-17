package com.seneca_brito.lab_manager.shared.DTOs.inventarioDTOs;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InventarioUpdateDTO(
        @Size(min = 1, max = 100, message = "nome deve ter entre 1 e 100 caracteres") String nome,
        @PositiveOrZero(message = "quantidade disponivel deve ser positiva ou zero")
        Integer quantidadeDisponivel,
        @PositiveOrZero(message = "quantidade indisponivel deve ser positiva ou zero")
        Integer quantidadeIndisponivel,
        UUID laboratorioId
) {
}
