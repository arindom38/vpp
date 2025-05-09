package com.challenge.vpp.exception.handler;

import com.challenge.vpp.dto.ErrorResponse;
import com.challenge.vpp.exception.BatteryDataException;
import com.challenge.vpp.exception.InvalidCapacityRangeException;
import com.challenge.vpp.exception.InvalidPostcodeRangeException;
import com.challenge.vpp.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidPostcodeRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPostcodeRange(
            InvalidPostcodeRangeException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false)
        );
    }
    
    @ExceptionHandler(InvalidCapacityRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCapacityRange(
            InvalidCapacityRangeException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(BatteryDataException.class)
    public ResponseEntity<ErrorResponse> handleBatteryDataException(
            BatteryDataException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String key = fieldName.contains("[") ? 
                    fieldName.substring(fieldName.indexOf("[") + 1, fieldName.indexOf("]")) + "." + 
                    fieldName.substring(fieldName.indexOf(".") + 1) : 
                    fieldName;
            String message = error.getDefaultMessage();
            errors.merge(key, message, (existing, newMsg) -> existing + ", " + newMsg);
        });

        String errorMessage = errors.entrySet().stream()
                .map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("; "));

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed - " + errorMessage,
                request.getDescription(false)
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getDescription(false)
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(
            MissingServletRequestParameterException ex, WebRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getDescription(false)
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request body: " + ex.getMessage(),
                request.getDescription(false)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request.getDescription(false)
        );
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(
            HttpStatus status, String message, String path) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
        
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            TypeMismatchException ex, WebRequest request) {
        String message = String.format(
                "Failed to convert '%s' to required type '%s'",
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown"
        );
        
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getDescription(false)
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        return createErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getDescription(false)
        );
    }


}