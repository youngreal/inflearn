package com.example.musinsa.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomMessagingException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;

    public CustomMessagingException() {
        super();
        this.message = "custom messaging event";
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
