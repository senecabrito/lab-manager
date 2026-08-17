package com.seneca_brito.lab_manager.shared.exceptions.errosDTOs;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ErroResponse(
        int status,
        String message,
        List<ErroCampo> erro
) {
    public static ErroResponse respostaPadrao(String mensagem){
        return new ErroResponse(HttpStatus.BAD_REQUEST.value(), mensagem, List.of());
    }

    public static ErroResponse of(HttpStatus status, String mensagem) {
        return new ErroResponse(status.value(), mensagem, List.of());
    }

}
