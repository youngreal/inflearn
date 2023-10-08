package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CannotCreateReplyException extends RuntimeException {
    private static final String message = "답글에는 답글을 작성할수 없다";
    private final HttpStatus httpStatus;

    public CannotCreateReplyException() {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
