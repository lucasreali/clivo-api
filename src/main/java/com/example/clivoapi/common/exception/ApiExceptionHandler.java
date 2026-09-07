package com.example.clivoapi.common.exception;

import com.example.clivoapi.common.exception.ErrorResponse.FieldViolation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleBusinessRule(BusinessException exception, HttpServletRequest request) {
        return ErrorResponse.of(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), pathOf(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleRejectedValue(IllegalArgumentException exception, HttpServletRequest request) {
        return ErrorResponse.of(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage(), pathOf(request));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleMissingResource(ResourceNotFoundException exception, HttpServletRequest request) {
        return ErrorResponse.of(HttpStatus.NOT_FOUND, exception.getMessage(), pathOf(request));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbiddenOperation(ForbiddenOperationException exception, HttpServletRequest request) {
        return ErrorResponse.of(HttpStatus.FORBIDDEN, exception.getMessage(), pathOf(request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidPayload(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return ErrorResponse.withViolations(
                HttpStatus.BAD_REQUEST, "invalid request payload", pathOf(request), violationsOf(exception.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleViolatedConstraint(
            ConstraintViolationException exception, HttpServletRequest request) {
        return ErrorResponse.withViolations(
                HttpStatus.BAD_REQUEST, "invalid request", pathOf(request), violationsOf(exception));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadablePayload(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        return ErrorResponse.of(HttpStatus.BAD_REQUEST, unreadableMessageOf(exception), pathOf(request));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMistypedParameter(
            MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return ErrorResponse.withViolations(
                HttpStatus.BAD_REQUEST,
                "invalid request",
                pathOf(request),
                List.of(new FieldViolation(exception.getName(), expectationOf(exception))));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParameter(
            MissingServletRequestParameterException exception, HttpServletRequest request) {
        return ErrorResponse.withViolations(
                HttpStatus.BAD_REQUEST,
                "invalid request",
                pathOf(request),
                List.of(new FieldViolation(exception.getParameterName(), "is required")));
    }

    private String pathOf(HttpServletRequest request) {
        return request.getRequestURI();
    }

    private String unreadableMessageOf(HttpMessageNotReadableException exception) {
        return "the request body could not be read: %s".formatted(rootCauseOf(exception).getMessage());
    }

    private Throwable rootCauseOf(Throwable failure) {
        return failure.getCause() == null ? failure : rootCauseOf(failure.getCause());
    }

    private String expectationOf(MethodArgumentTypeMismatchException exception) {
        return "expected a %s".formatted(exception.getRequiredType().getSimpleName());
    }

    private List<FieldViolation> violationsOf(BindingResult bindingResult) {
        return bindingResult.getFieldErrors()
                .stream()
                .map(this::toViolation)
                .toList();
    }

    private List<FieldViolation> violationsOf(ConstraintViolationException exception) {
        return exception.getConstraintViolations()
                .stream()
                .map(this::toViolation)
                .toList();
    }

    private FieldViolation toViolation(FieldError error) {
        return new FieldViolation(error.getField(), error.getDefaultMessage());
    }

    private FieldViolation toViolation(ConstraintViolation<?> violation) {
        return new FieldViolation(violation.getPropertyPath().toString(), violation.getMessage());
    }
}
