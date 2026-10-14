package com.bogdusik.chat.controller;

import com.bogdusik.chat.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Turns rejected REST requests into {@code 400 {"error": "..."}}, the shape the frontend reads. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse invalidBody(MethodArgumentNotValidException e) {
        String reason = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Invalid request");
        return new ErrorResponse(reason);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse unreadableBody(HttpMessageNotReadableException e) {
        return new ErrorResponse("Request body is not valid JSON");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse wrongParameterType(MethodArgumentTypeMismatchException e) {
        return new ErrorResponse("Parameter '" + e.getName() + "' has an invalid value");
    }
}
