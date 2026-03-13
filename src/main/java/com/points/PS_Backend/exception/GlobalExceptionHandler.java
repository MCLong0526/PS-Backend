package com.points.PS_Backend.exception;

import com.points.PS_Backend.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ApiResponse handleRuntimeException(RuntimeException ex) {

        return new ApiResponse(
                400,
                ex.getMessage(),
                null,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse handleException(Exception ex) {

        return new ApiResponse(
                500,
                "Internal server error",
                null,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse handleValidationException(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return new ApiResponse(
                400,
                message,
                null,
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse handleDuplicateException(DataIntegrityViolationException ex) {

        String message = "Duplicate data";

        if (ex.getMessage().contains("uk_users_email")) {
            message = "Email already registered";
        }

        if (ex.getMessage().contains("uk_users_phone")) {
            message = "Phone already registered";
        }

        return new ApiResponse(
                400,
                message,
                null,
                null
        );
    }
}