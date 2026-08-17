package com.seneca_brito.lab_manager.shared.exceptions.common;

import com.seneca_brito.lab_manager.shared.exceptions.errosDTOs.ErroCampo;
import com.seneca_brito.lab_manager.shared.exceptions.errosDTOs.ErroResponse;
import com.seneca_brito.lab_manager.shared.exceptions.RecursoNaoEncontradoException;
import com.seneca_brito.lab_manager.shared.exceptions.RegistroDuplicadoException;
import com.seneca_brito.lab_manager.shared.exceptions.ConflitoEstadoException;
import com.seneca_brito.lab_manager.shared.exceptions.RegraNegocioException;
import com.seneca_brito.lab_manager.shared.exceptions.UserNotAuthorizedException;
import com.seneca_brito.lab_manager.shared.exceptions.RequisicaoInvalidaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(ConflitoEstadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse handleConflict(ConflitoEstadoException e) {
        return ErroResponse.of(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErroResponse handleBusinessRule(RegraNegocioException e) {
        return ErroResponse.of(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErroResponse handleForbidden(UserNotAuthorizedException e) {
        return ErroResponse.of(HttpStatus.FORBIDDEN, e.getMessage());
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

    @ExceptionHandler(RequisicaoInvalidaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse handleInvalidRequest(RequisicaoInvalidaException e) {
        return ErroResponse.of(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, ServletRequestBindingException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse handleMissingParameter() {
        return ErroResponse.of(HttpStatus.BAD_REQUEST, "Requisicao invalida");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse handleNoResourceFound() {
        return ErroResponse.of(HttpStatus.NOT_FOUND, "Recurso nao encontrado");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroResponse handleUnexpectedException(Exception exception) {
        LOGGER.error("Unexpected request failure", exception);
        return ErroResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }
}
