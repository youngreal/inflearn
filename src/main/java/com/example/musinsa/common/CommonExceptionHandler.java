package com.example.musinsa.common;

import com.example.musinsa.common.exception.AlreadyExistMemberException;
import com.example.musinsa.common.exception.DoesNotExistEmailException;
import com.example.musinsa.common.exception.DoesNotExistMemberException;
import com.example.musinsa.common.exception.EmptyCookieRequestException;
import com.example.musinsa.common.exception.UnAuthorizationException;
import com.example.musinsa.common.exception.WrongEmailTokenException;
import com.example.musinsa.common.exception.WrongServletRequestException;
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

    @ExceptionHandler(AlreadyExistMemberException.class)
    public ResponseEntity<String> alreadyExistMemberException(AlreadyExistMemberException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(DoesNotExistEmailException.class)
    public ResponseEntity<String> doesNotExistEmailException(DoesNotExistEmailException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(DoesNotExistMemberException.class)
    public ResponseEntity<String> doesNotExistMemberException(DoesNotExistMemberException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(WrongEmailTokenException.class)
    public ResponseEntity<String> wrongEmailTokenException(WrongEmailTokenException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(UnAuthorizationException.class)
    public ResponseEntity<String> unAuthorizationException(UnAuthorizationException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(WrongServletRequestException.class)
    public ResponseEntity<String> wrongServletRequestException(WrongServletRequestException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(EmptyCookieRequestException.class)
    public ResponseEntity<String> emptyCookieRequestException(EmptyCookieRequestException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> constraintViolationException() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("잘못된 폼 입력값 입니다");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> runtimeException() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("server error");
    }
}
