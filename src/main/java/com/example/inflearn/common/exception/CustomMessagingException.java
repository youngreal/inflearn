package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomMessagingException extends RuntimeException {
    private final HttpStatus httpStatus;
    private static final String RESPONSE_MESSAGE = "custom messaging event";

    public CustomMessagingException(Throwable e) {
        super(RESPONSE_MESSAGE, e);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
