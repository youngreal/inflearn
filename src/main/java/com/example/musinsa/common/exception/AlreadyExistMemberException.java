package com.example.musinsa.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AlreadyExistMemberException extends RuntimeException {
    private static final String message = "이미 존재하는 회원입니다";
    private final HttpStatus httpStatus;

    public AlreadyExistMemberException() {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
