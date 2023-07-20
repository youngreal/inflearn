package com.example.musinsa.common.exception;

import org.springframework.http.HttpStatus;

public class DoesNotExistPostException extends RuntimeException{
    private final String message = "존재하지 않는 게시글입니다";
    private final HttpStatus httpStatus;

    public DoesNotExistPostException(String message) {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
