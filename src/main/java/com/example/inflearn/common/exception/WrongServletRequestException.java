package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WrongServletRequestException extends RuntimeException {
    private static final String MESSAGE = "잘못된 서블릿 요청입니다";
    private final HttpStatus httpStatus;

    public WrongServletRequestException() {
        super(MESSAGE);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
