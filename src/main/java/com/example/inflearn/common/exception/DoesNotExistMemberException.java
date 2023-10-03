package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DoesNotExistMemberException extends RuntimeException {
    private static final String RESPONSE_MESSAGE = "존재하지 않는 유저입니다";
    private final HttpStatus httpStatus;

    public DoesNotExistMemberException() {
       super(RESPONSE_MESSAGE);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
