package com.marques.estoque.infra;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@AllArgsConstructor
@Getter @Setter
public class StandardError {
    private HttpStatus httpStatus;
    private String message;
    private Instant timestamp;

}
