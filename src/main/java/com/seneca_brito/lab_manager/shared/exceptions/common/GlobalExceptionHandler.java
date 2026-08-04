package com.seneca_brito.lab_manager.shared.exceptions.common;

import com.seneca_brito.lab_manager.shared.exceptions.errosDTOs.ErroCampo;
import com.seneca_brito.lab_manager.shared.exceptions.errosDTOs.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErroResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<FieldError> fieldErrorList = e.getFieldErrors();
        List<ErroCampo> listaErros = fieldErrorList
                .stream()
                .map(fe -> new ErroCampo(fe.getField(), fe.getDefaultMessage()))
                .collect((Collectors.toList()));
        return new ErroResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Erro de validacao",
                listaErros);
    }
}
