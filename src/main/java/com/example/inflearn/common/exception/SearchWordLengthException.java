package com.example.inflearn.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SearchWordLengthException extends RuntimeException {
    private static final String message = "2글자 이상의 검색어를 입력해주세요";
    private final HttpStatus httpStatus;

    public SearchWordLengthException() {
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }
}
