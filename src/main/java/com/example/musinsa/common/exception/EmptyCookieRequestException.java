package com.example.musinsa.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmptyCookieRequestException extends RuntimeException {
    private final String message;
    private final HttpStatus httpStatus;

    public EmptyCookieRequestException(String message) {
        super(message);
        this.message = message;
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
}
