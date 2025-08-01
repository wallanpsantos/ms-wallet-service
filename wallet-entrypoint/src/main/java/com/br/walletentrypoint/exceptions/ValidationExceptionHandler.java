package com.br.walletentrypoint.exceptions;

import com.br.walletcore.exceptions.response.ErrorDetailsMessage;
import com.br.walletcore.exceptions.response.ErrorMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
@Slf4j
public class ValidationExceptionHandler {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation failed for request: {}", ex.getMessage());

        List<ErrorDetailsMessage> details = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.add(new ErrorDetailsMessage(fieldName, errorMessage));
        });

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields have validation errors",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                details
        );

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessage> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        List<ErrorDetailsMessage> details = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            details.add(new ErrorDetailsMessage(fieldName, errorMessage));
        }

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value(),
                "Constraint Violation",
                "One or more constraints were violated",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                details
        );

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorMessage> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Invalid JSON format: {}", ex.getMessage());

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request Body",
                "The request body contains invalid JSON format or missing required fields",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                Collections.singletonList(new ErrorDetailsMessage("requestBody", "Invalid JSON format"))
        );

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch for parameter: {}", ex.getName());

        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = String.format("Parameter '%s' should be of type '%s'", ex.getName(), expectedType);

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Parameter Type",
                "One or more parameters have invalid type",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                Collections.singletonList(new ErrorDetailsMessage(ex.getName(), message))
        );

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorMessage> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("Missing required parameter: {}", ex.getParameterName());

        String message = String.format("Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value(),
                "Missing Required Parameter",
                "One or more required parameters are missing",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                Collections.singletonList(new ErrorDetailsMessage(ex.getParameterName(), message))
        );

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Business rule violation: {}", ex.getMessage());

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                HttpStatus.BAD_REQUEST.value(),
                "Business Rule Violation",
                ex.getMessage(),
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                Collections.singletonList(new ErrorDetailsMessage("businessRule", ex.getMessage()))
        );

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorMessage> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error occurred: ", ex);

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred while processing the request",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                Collections.singletonList(new ErrorDetailsMessage("system", "Internal server error"))
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: ", ex);

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected Error",
                "An unexpected error occurred while processing the request",
                LocalDateTime.now().format(TIMESTAMP_FORMATTER),
                Collections.singletonList(new ErrorDetailsMessage("system", "Unexpected error occurred"))
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }
}