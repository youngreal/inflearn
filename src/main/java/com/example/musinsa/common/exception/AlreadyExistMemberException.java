package com.example.musinsa.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AlreadyExistMemberException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;

    public AlreadyExistMemberException(String message) {
        super(message);
        this.message = message;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
