package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DuplicatedHashtagException extends RuntimeException {
    private static final String message = "중복된 해시태그가 존재합니다";
    private final HttpStatus httpStatus;

    public DuplicatedHashtagException() {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
