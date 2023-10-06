package com.example.inflearn.common;

import com.example.inflearn.common.exception.AlreadyExistMemberException;
import com.example.inflearn.common.exception.AlreadyLikeException;
import com.example.inflearn.common.exception.CustomMessagingException;
import com.example.inflearn.common.exception.DoesNotExistEmailException;
import com.example.inflearn.common.exception.DoesNotExistMemberException;
import com.example.inflearn.common.exception.DoesNotLikeException;
import com.example.inflearn.common.exception.DuplicatedHashtagException;
import com.example.inflearn.common.exception.EmptyCookieRequestException;
import com.example.inflearn.common.exception.SearchWordLengthException;
import com.example.inflearn.common.exception.UnAuthorizationException;
import com.example.inflearn.common.exception.WrongEmailTokenException;
import com.example.inflearn.common.exception.WrongServletRequestException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
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

    @ExceptionHandler(DuplicatedHashtagException.class)
    public ResponseEntity<String> duplicatedHashtagException(DuplicatedHashtagException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(CustomMessagingException.class)
    public ResponseEntity<String> customMessagingException(CustomMessagingException e) {
        log.info("custom ex 핸들링");
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(SearchWordLengthException.class)
    public ResponseEntity<String> searchWordLengthException(SearchWordLengthException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(AlreadyLikeException.class)
    public ResponseEntity<String> alreadyLikeException(AlreadyLikeException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }

    @ExceptionHandler(DoesNotLikeException.class)
    public ResponseEntity<String> doesNotLikeException(DoesNotLikeException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(e.getMessage());
    }
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<String> exception(RuntimeException e) {
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(e.getMessage());
//    }
}
