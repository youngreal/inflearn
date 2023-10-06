package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DoesNotLikeException extends RuntimeException {
    private static final String message = "좋아요 누르지 않은 게시글입니다";
    private final HttpStatus httpStatus;

    public DoesNotLikeException() {
        super(message);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
