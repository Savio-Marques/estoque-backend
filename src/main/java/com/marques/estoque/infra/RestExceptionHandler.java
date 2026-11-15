package com.marques.estoque.infra;

import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.GeneralException;
import com.marques.estoque.exception.NotFoundException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Dados inválidos na requisição.";
        }
        StandardError err = new StandardError(HttpStatus.BAD_REQUEST, message, Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(AuthenticationException.class)
    private ResponseEntity<StandardError> authenticationExceptionHandler(AuthenticationException exception) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandardError err = new StandardError(status, "Usuário ou senha inválidos.", Instant.now());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(NotFoundException.class)
    private ResponseEntity<StandardError> notFoundHandler (NotFoundException exception) {
        String message = exception.getMessage();
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardError err = new StandardError(status, message, Instant.now());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ArgumentException.class)
    private ResponseEntity<StandardError> argumentExceptionHandler (ArgumentException exception) {
        String message = exception.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardError err = new StandardError(status, message, Instant.now());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(GeneralException.class)
    private ResponseEntity<StandardError> argumentExceptionHandler (GeneralException exception) {
        String message = exception.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardError err = new StandardError(status, message, Instant.now());
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(DuplicateDataException.class)
    private ResponseEntity<StandardError> duplicatedDataHandler (DuplicateDataException exception) {
        String message = exception.getMessage();
        HttpStatus status = HttpStatus.CONFLICT;
        StandardError err = new StandardError(status, message, Instant.now());
        return ResponseEntity.status(status).body(err);
    }
}
