package com.study.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("{} is occurred", e.getErrorCode());

        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(new ErrorResponse(e.getErrorCode().getHttpStatus(), e.getErrorCode().name(),
                e.getErrorCode().getMessage()));
    }


    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {

        private final HttpStatus status;
        private final String code;
        private final String message;

    }
}
