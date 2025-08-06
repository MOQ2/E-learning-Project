package com.example.e_learning_system.excpetions;

import com.example.e_learning_system.DTOs.BaseErrorResponse;
import com.example.e_learning_system.DTOs.SecurityErrorResponse;
import com.example.e_learning_system.DTOs.ValidationErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ========== CUSTOM EXCEPTIONS ==========

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.error("Base exception occurred: {}", ex.getMessage(), ex);

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getStatus(),
                request.getRequestURI(),
                request.getMethod()
        );


        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<BaseErrorResponse> handleResourceNotFoundException(ResourceNotFound ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getStatus(),
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<BaseErrorResponse> handleClientException(ClientException ex, HttpServletRequest request) {
        log.warn("Client exception: {}", ex.getMessage());

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getStatus(),
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        // Add arguments as metadata if available
        if (ex.getArguments() != null && ex.getArguments().length > 0) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("arguments", Arrays.asList(ex.getArguments()));
            errorResponse.setMetadata(metadata);
        }

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(ValidationException ex, HttpServletRequest request) {
        log.warn("Validation exception: {}", ex.getMessage());

        ValidationErrorResponse errorResponse = ValidationErrorResponse.from(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getStatus(),
                request.getRequestURI(),
                request.getMethod(),
                ex.getValidationErrorsMessages()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<SecurityErrorResponse> handleSecurityException(SecurityException ex, HttpServletRequest request) {
        log.error("Security exception: {}", ex.getMessage(), ex);

        SecurityErrorResponse errorResponse = SecurityErrorResponse.from(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getStatus(),
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<BaseErrorResponse> handleServiceException(ServiceException ex, HttpServletRequest request) {
        log.error("Service exception: {}", ex.getMessage(), ex);

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getStatus(),
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    // ========== SPRING VALIDATION EXCEPTIONS ==========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Method argument validation failed: {}", ex.getMessage());

        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        ValidationErrorResponse errorResponse = ValidationErrorResponse.from(
                "Validation failed for request body",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                request.getMethod(),
                validationErrors
        );

        errorResponse.setFieldErrors(fieldErrors);
        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {
        log.warn("Binding exception: {}", ex.getMessage());

        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = ValidationErrorResponse.from(
                "Validation failed for request parameters",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                request.getMethod(),
                validationErrors
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());

        List<String> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = ValidationErrorResponse.from(
                "Constraint validation failed",
                "CONSTRAINT_VIOLATION",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                request.getMethod(),
                validationErrors
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ========== SPRING SECURITY EXCEPTIONS ==========

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<SecurityErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        SecurityErrorResponse errorResponse = SecurityErrorResponse.from(
                "Authentication failed",
                "AUTHENTICATION_FAILED",
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<SecurityErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());

        SecurityErrorResponse errorResponse = SecurityErrorResponse.from(
                "Invalid username or password",
                "BAD_CREDENTIALS",
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<SecurityErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());

        SecurityErrorResponse errorResponse = SecurityErrorResponse.from(
                "Access denied",
                "ACCESS_DENIED",
                HttpStatus.FORBIDDEN,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<SecurityErrorResponse> handleInsufficientAuthentication(InsufficientAuthenticationException ex, HttpServletRequest request) {
        log.warn("Insufficient authentication: {}", ex.getMessage());

        SecurityErrorResponse errorResponse = SecurityErrorResponse.from(
                "Full authentication is required to access this resource",
                "INSUFFICIENT_AUTHENTICATION",
                HttpStatus.UNAUTHORIZED,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // ========== HTTP EXCEPTIONS ==========

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not supported: {}", ex.getMessage());

        String supportedMethods = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "Unknown";

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                String.format("Method '%s' not supported. Supported methods: %s", ex.getMethod(), supportedMethods),
                "METHOD_NOT_SUPPORTED",
                HttpStatus.METHOD_NOT_ALLOWED,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<BaseErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn("Media type not supported: {}", ex.getMessage());

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                "Media type not supported: " + ex.getContentType(),
                "MEDIA_TYPE_NOT_SUPPORTED",
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Message not readable: {}", ex.getMessage());

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                "Malformed JSON request or invalid request body",
                "MESSAGE_NOT_READABLE",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing request parameter: {}", ex.getMessage());

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                String.format("Required parameter '%s' is missing", ex.getParameterName()),
                "MISSING_PARAMETER",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<BaseErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch: {}", ex.getMessage());

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                String.format("Parameter '%s' should be of type %s", ex.getName(), ex.getRequiredType().getSimpleName()),
                "TYPE_MISMATCH",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found: {}", ex.getMessage());

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()),
                "NO_HANDLER_FOUND",
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<BaseErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("File upload size exceeded: {}", ex.getMessage());

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                "File upload size exceeds the maximum allowed limit",
                "FILE_SIZE_EXCEEDED",
                HttpStatus.PAYLOAD_TOO_LARGE,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // ========== DATABASE EXCEPTIONS ==========

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Data integrity violation: {}", ex.getMessage(), ex);

        String message = "Data integrity constraint violation";
        String errorCode = "DATA_INTEGRITY_VIOLATION";

        // Check for common constraint violations
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique constraint") || ex.getMessage().contains("Duplicate entry")) {
                message = "Resource already exists with the provided unique field";
                errorCode = "DUPLICATE_RESOURCE";
            } else if (ex.getMessage().contains("foreign key constraint")) {
                message = "Referenced resource does not exist";
                errorCode = "FOREIGN_KEY_VIOLATION";
            } else if (ex.getMessage().contains("not-null constraint")) {
                message = "Required field is missing";
                errorCode = "NOT_NULL_VIOLATION";
            }
        }

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                message,
                errorCode,
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<BaseErrorResponse> handleDataAccessException(DataAccessException ex, HttpServletRequest request) {
        log.error("Data access exception: {}", ex.getMessage(), ex);

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                "Database operation failed",
                "DATABASE_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== GENERAL EXCEPTION ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        BaseErrorResponse errorResponse = BaseErrorResponse.from(
                "An unexpected error occurred",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                request.getMethod()
        );

        errorResponse.setRequestId(generateRequestId());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== UTILITY METHODS ==========

    private String generateRequestId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}