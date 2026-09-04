package com.example.clivoapi.common.exception;

import com.example.clivoapi.common.exception.ErrorResponse.FieldViolation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleBusinessRule(BusinessException exception, HttpServletRequest request) {
        return ErrorResponse.of(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), pathOf(request));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleMissingResource(ResourceNotFoundException exception, HttpServletRequest request) {
        return ErrorResponse.of(HttpStatus.NOT_FOUND, exception.getMessage(), pathOf(request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidPayload(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return ErrorResponse.withViolations(
                HttpStatus.BAD_REQUEST, "invalid request payload", pathOf(request), violationsOf(exception.getBindingResult()));
    }

    private String pathOf(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private List<FieldViolation> violationsOf(BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
                .stream()
                .map(this::toViolation)
                .toList();
    }

    private FieldViolation toViolation(FieldError error) {
        return new FieldViolation(error.getField(), error.getDefaultMessage());
    }
}
