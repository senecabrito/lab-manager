package com.seneca_brito.lab_manager.shared.exceptions.common;

import com.seneca_brito.lab_manager.shared.exceptions.errosDTOs.ErroCampo;
import com.seneca_brito.lab_manager.shared.exceptions.errosDTOs.ErroResponse;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.RegistroDuplicadoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErroResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<FieldError> fieldErrorList = e.getFieldErrors();
        List<ErroCampo> listaErros = fieldErrorList
                .stream()
                .map(fe -> new ErroCampo(fe.getField(), fe.getDefaultMessage()))
                .collect((Collectors.toList()));
        return new ErroResponse(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                "Erro de validacao",
                listaErros);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse handleNotFound(RecursoNaoEncontradoException e) {
        return ErroResponse.of(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(RegistroDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse handleDuplicate(RegistroDuplicadoException e) {
        return ErroResponse.of(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse handleDataIntegrityViolation() {
        return ErroResponse.of(HttpStatus.CONFLICT,
                "Operacao conflitante com a integridade dos dados");
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse handleBadRequest() {
        return ErroResponse.of(HttpStatus.BAD_REQUEST, "Requisicao invalida");
    }
}
