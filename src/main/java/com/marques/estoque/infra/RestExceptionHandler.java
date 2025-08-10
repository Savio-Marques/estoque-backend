package com.marques.estoque.infra;

import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.GeneralException;
import com.marques.estoque.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

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
