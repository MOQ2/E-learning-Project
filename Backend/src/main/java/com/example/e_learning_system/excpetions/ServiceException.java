package com.example.e_learning_system.excpetions;

import org.springframework.http.HttpStatus;

public class ServiceException extends BaseException {

    public ServiceException(String message, String errorCode, HttpStatus status) {
        super(message, errorCode, status);
    }

    public ServiceException(String message, String errorCode, HttpStatus status, Object[] arguments) {
        super(message, errorCode, status, arguments);
    }

    public ServiceException(String message, String errorCode, HttpStatus status, Throwable cause, Object[] arguments) {
        super(message, errorCode, status, cause, arguments);
    }


    public static ServiceException databaseError(String operation) {
        return new ServiceException("Database error during: " + operation, "DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException externalServiceUnavailable(String serviceName) {
        return new ServiceException("External service unavailable: " + serviceName, "EXTERNAL_SERVICE_UNAVAILABLE", HttpStatus.SERVICE_UNAVAILABLE);
    }

    public static ServiceException configurationError(String configItem) {
        return new ServiceException("Configuration error for: " + configItem, "CONFIGURATION_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException fileProcessingError(String fileName) {
        return new ServiceException("Error processing file: " + fileName, "FILE_PROCESSING_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException emailServiceError(String recipient) {
        return new ServiceException("Failed to send email to: " + recipient, "EMAIL_SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException paymentProcessingError(String transactionId) {
        return new ServiceException("Payment processing failed for transaction: " + transactionId, "PAYMENT_PROCESSING_ERROR", HttpStatus.PAYMENT_REQUIRED);
    }

    public static ServiceException enrollmentServiceError(String courseId, String studentId) {
        return new ServiceException("Enrollment failed for student " + studentId + " in course " + courseId, "ENROLLMENT_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException gradeCalculationError(String quizId) {
        return new ServiceException("Grade calculation failed for quiz: " + quizId, "GRADE_CALCULATION_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException resourceUploadError(String resourceType) {
        return new ServiceException("Failed to upload " + resourceType, "RESOURCE_UPLOAD_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException cacheServiceError(String operation) {
        return new ServiceException("Cache service error during: " + operation, "CACHE_SERVICE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static ServiceException businessRuleViolation(String rule, String details) {
        return new ServiceException("Business rule violation - " + rule + ": " + details, "BUSINESS_RULE_VIOLATION", HttpStatus.CONFLICT);
    }

    public static ServiceException concurrencyError(String resource) {
        return new ServiceException("Concurrent modification detected for: " + resource, "CONCURRENCY_ERROR", HttpStatus.CONFLICT);
    }
}