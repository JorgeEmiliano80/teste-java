package com.jorge.portfolio.common.exception;

import com.jorge.portfolio.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BAD_REQUEST_ERROR = "Requisição inválida";
    private static final String NOT_FOUND_ERROR = "Recurso não encontrado";
    private static final String INTERNAL_SERVER_ERROR = "Erro interno";

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                ErrorResponse.of(
                        status.value(),
                        BAD_REQUEST_ERROR,
                        exception.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        return ResponseEntity.status(status).body(
                ErrorResponse.of(
                        status.value(),
                        NOT_FOUND_ERROR,
                        exception.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(status).body(
                ErrorResponse.withFieldErrors(
                        status.value(),
                        BAD_REQUEST_ERROR,
                        "Erro de validação.",
                        request.getRequestURI(),
                        fieldErrors
                )
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                ErrorResponse.of(
                        status.value(),
                        BAD_REQUEST_ERROR,
                        "Parâmetro inválido.",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(
                ErrorResponse.of(
                        status.value(),
                        INTERNAL_SERVER_ERROR,
                        "Erro interno inesperado.",
                        request.getRequestURI()
                )
        );
    }
}
