package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DoesNotExistCommentException extends  RuntimeException {
    private static final String message = "존재하지 않는 댓글입니다";
    private final HttpStatus httpStatus;

    public DoesNotExistCommentException() {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
