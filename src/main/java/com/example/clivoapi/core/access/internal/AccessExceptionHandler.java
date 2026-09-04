package com.example.clivoapi.core.access.internal;

import com.example.clivoapi.common.exception.ErrorResponse;
import com.example.clivoapi.core.access.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class AccessExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ErrorResponse handleRejectedCredentials(InvalidCredentialsException exception, HttpServletRequest request) {
        return ErrorResponse.of(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI());
    }
}
