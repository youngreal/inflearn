package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WrongEmailTokenException extends RuntimeException {
    private final String message;
    private final HttpStatus httpStatus;

    public WrongEmailTokenException(String message) {
        super(message);
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
