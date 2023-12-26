package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmptyCookieRequestException extends RuntimeException {
    private static final String MESSAGE = "잘못된 요청입니다";
    private final HttpStatus httpStatus;

    public EmptyCookieRequestException() {
        super(MESSAGE);
        this.httpStatus = HttpStatus.UNAUTHORIZED;
    }
}
