package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AlreadyLikeException extends RuntimeException {
    private static final String MESSAGE = "이미 좋아요한 게시글입니다";
    private final HttpStatus httpStatus;

    public AlreadyLikeException() {
        super(MESSAGE);
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
