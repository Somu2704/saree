package com.saree.common.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();
        log.warn("Validation failed: {}", details, ex);
        return ResponseEntity.badRequest().body(buildErrorBody("VALIDATION_ERROR", "Request validation failed", details));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildErrorBody("AUTHENTICATION_ERROR", ex.getMessage(), List.of()));
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(RuntimeException ex) {
        log.warn("Invalid request: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(buildErrorBody("INVALID_REQUEST", ex.getMessage(), List.of()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("Request body is malformed or missing", ex);
        return ResponseEntity.badRequest().body(buildErrorBody("INVALID_REQUEST", "Request body is malformed or missing", List.of()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch for parameter {}: {}", ex.getName(), ex.getValue(), ex);
        return ResponseEntity.badRequest().body(buildErrorBody("INVALID_REQUEST", "Invalid value for parameter: " + ex.getName(), List.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Database integrity violation", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(buildErrorBody("CONFLICT", "Duplicate or invalid data submitted", List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("Unhandled exception in API layer", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorBody("INTERNAL_SERVER_ERROR", "An unexpected error occurred", List.of()));
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private Map<String, Object> buildErrorBody(String code, String message, List<String> details) {
        return Map.of("error", Map.of(
                "code", code,
                "message", message,
                "details", details
        ));
    }
}
