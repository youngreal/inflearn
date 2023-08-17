package com.example.musinsa.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DoesNotExistMemberException extends RuntimeException {
    private final String message;
    private final HttpStatus httpStatus;

    public DoesNotExistMemberException() {
       super();
        this.message = "존재하지 않는 유저입니다";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
