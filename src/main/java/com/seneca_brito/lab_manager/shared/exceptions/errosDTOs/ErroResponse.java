package com.seneca_brito.lab_manager.shared.exceptions.errosDTOs;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.util.List;

public record ErroResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "422") int status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Erro de validacao") String message,
        @ArraySchema(arraySchema = @Schema(requiredMode = Schema.RequiredMode.REQUIRED)) List<ErroCampo> erro
) {
    public static ErroResponse respostaPadrao(String mensagem){
        return new ErroResponse(HttpStatus.BAD_REQUEST.value(), mensagem, List.of());
    }

    public static ErroResponse of(HttpStatus status, String mensagem) {
        return new ErroResponse(status.value(), mensagem, List.of());
    }

}
