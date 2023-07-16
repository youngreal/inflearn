package com.example.musinsa.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WrongServletRequestException extends RuntimeException {
    private final String message;
    private final HttpStatus httpStatus;

    public WrongServletRequestException(String message) {
        super(message);
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
