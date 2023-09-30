package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UnAuthorizationException extends RuntimeException {

    private final String message;
    private final HttpStatus httpStatus;

    public UnAuthorizationException(String message) {
        super(message);
        this.message = message;
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }

}
