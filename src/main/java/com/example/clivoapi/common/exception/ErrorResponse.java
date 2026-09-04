package com.example.clivoapi.common.exception;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> violations) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return withViolations(status, message, path, List.of());
    }

    public static ErrorResponse withViolations(
            HttpStatus status, String message, String path, List<FieldViolation> violations) {
        return new ErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, path, violations);
    }

    public record FieldViolation(String field, String message) {
    }
}
