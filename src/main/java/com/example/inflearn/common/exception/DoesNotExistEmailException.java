package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DoesNotExistEmailException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;

    public DoesNotExistEmailException(String message) {
        super(message);
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
