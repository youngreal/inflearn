package com.example.musinsa.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionHandler {

    //todo 공통 예외형식이 필요하다면 그때 리팩토링하자.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> methodArgumentNotValidException() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("잘못된 폼 입력값 입니다");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> constraintViolationException() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("잘못된 폼 입력값 입니다");
    }
}
