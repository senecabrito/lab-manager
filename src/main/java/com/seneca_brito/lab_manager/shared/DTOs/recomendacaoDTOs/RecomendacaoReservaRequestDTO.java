package com.seneca_brito.lab_manager.shared.DTOs.recomendacaoDTOs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record RecomendacaoReservaRequestDTO(
        @NotNull(message = "Campo obrigatorio") LocalDate data,
        @Schema(type = "string", format = "time", example = "10:00:00")
        LocalTime horarioPreferencial,
        @NotNull(message = "Campo obrigatorio")
        @Positive(message = "duracao deve ser positiva") Integer duracaoMinutos,
        @NotNull(message = "Campo obrigatorio")
        @Positive(message = "quantidade de alunos deve ser positiva") Integer quantidadeAlunos,
        Set<@NotBlank(message = "recurso nao pode estar em branco")
                @Size(max = 100, message = "recurso deve ter no maximo 100 caracteres") String> recursos,
        @Size(max = 120, message = "localizacao deve ter no maximo 120 caracteres") String localizacao,
        UUID laboratorioId
) {
}
