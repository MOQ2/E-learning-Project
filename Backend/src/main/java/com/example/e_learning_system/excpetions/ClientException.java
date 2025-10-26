package com.example.e_learning_system.excpetions;

import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ClientException extends BaseException {

    public ClientException(String message, String errorCode, HttpStatus status) {
        super(message, errorCode, status);
    }

    public ClientException(String message, String errorCode, HttpStatus status, Object[] arguments) {
        super(message, errorCode, status, arguments);
    }

    public ClientException(String message, String errorCode, HttpStatus status, Throwable cause, Object[] arguments) {
        super(message, errorCode, status, cause, arguments);
    }

    // ========== BASIC CLIENT ERRORS ==========
    public static ClientException badRequest(String message) {
        return new ClientException(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    public static ClientException unauthorized(String message) {
        return new ClientException(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }

    public static ClientException forbidden(String resource) {
        return new ClientException("Access denied to resource: " + resource, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }

    public static ClientException invalidCredentials() {
        return new ClientException("Invalid username or password", "INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED);
    }

    public static ClientException accountLocked(String username) {
        return new ClientException("Account is locked for user: " + username, "ACCOUNT_LOCKED", HttpStatus.FORBIDDEN);
    }

    public static ClientException sessionExpired() {
        return new ClientException("Session has expired", "SESSION_EXPIRED", HttpStatus.UNAUTHORIZED);
    }

    public static ClientException insufficientPermissions(String action) {
        return new ClientException("Insufficient permissions to perform: " + action, "INSUFFICIENT_PERMISSIONS", HttpStatus.FORBIDDEN);
    }

    // ========== ACCESS CONTROL ERRORS ==========
    public static ClientException accessDenied(String action, String resourceType, Object resourceId) {
        return new ClientException(
                String.format("Access denied for action '%s' on %s '%s'", action, resourceType.toLowerCase(), resourceId),
                "ACCESS_DENIED", HttpStatus.FORBIDDEN);
    }

    public static ClientException insufficientPermission(String requiredPermission, String resourceType, Object resourceId) {
        return new ClientException(
                String.format("Insufficient permission. Required: %s for %s %s", requiredPermission, resourceType.toLowerCase(), resourceId),
                "INSUFFICIENT_PERMISSION", HttpStatus.FORBIDDEN);
    }

    // ========== BUSINESS RULE VIOLATIONS ==========
    public static ClientException businessRuleViolation(String message, String ruleCode) {
        return new ClientException(message, ruleCode, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public static ClientException duplicateResource(String resourceType, String field, Object value) {
        return new ClientException(
                String.format("%s with %s '%s' already exists", resourceType, field, value),
                "DUPLICATE_RESOURCE", HttpStatus.CONFLICT);
    }

    public static ClientException invalidStatus(String resourceType, Object resourceId, String currentStatus, String requiredStatus) {
        return new ClientException(
                String.format("%s %s has status '%s' but requires '%s'", resourceType, resourceId, currentStatus, requiredStatus),
                "INVALID_STATUS", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ========== ENROLLMENT ERRORS ==========
    public static ClientException alreadyEnrolled(Long userId, Long courseId) {
        return new ClientException(
                String.format("User %d is already enrolled in course %d", userId, courseId),
                "ALREADY_ENROLLED", HttpStatus.CONFLICT);
    }

    public static ClientException enrollmentLimitExceeded(Long courseId, int limit, int current) {
        return new ClientException(
                String.format("Course %d has reached enrollment limit. Limit: %d, Current: %d", courseId, limit, current),
                "ENROLLMENT_LIMIT_EXCEEDED", HttpStatus.CONFLICT);
    }

    public static ClientException enrollmentNotAllowed(Long userId, Long courseId, String reason) {
        return new ClientException(
                String.format("Enrollment not allowed for user %d in course %d: %s", userId, courseId, reason),
                "ENROLLMENT_NOT_ALLOWED", HttpStatus.FORBIDDEN);
    }

    // ========== SUBSCRIPTION ERRORS ==========
    public static ClientException subscriptionRequired(Long courseId) {
        return new ClientException(
                String.format("Active subscription required to access course %d", courseId),
                "SUBSCRIPTION_REQUIRED", HttpStatus.PAYMENT_REQUIRED);
    }

    public static ClientException subscriptionExpired(Long userId, Long courseId, LocalDateTime expiredAt) {
        return new ClientException(
                String.format("Subscription expired for user %d on course %d at %s", userId, courseId, expiredAt),
                "SUBSCRIPTION_EXPIRED", HttpStatus.PAYMENT_REQUIRED);
    }

    public static ClientException activeSubscriptionExists(Long userId, Long courseId) {
        return new ClientException(
                String.format("User %d already has active subscription for course %d", userId, courseId),
                "ACTIVE_SUBSCRIPTION_EXISTS", HttpStatus.CONFLICT);
    }

    // ========== QUIZ & ASSESSMENT ERRORS ==========
    public static ClientException maxAttemptsExceeded(Long quizId, int maxAttempts, int currentAttempts) {
        return new ClientException(
                String.format("Maximum attempts (%d) exceeded for quiz %d. Current attempts: %d", maxAttempts, quizId, currentAttempts),
                "MAX_ATTEMPTS_EXCEEDED", HttpStatus.CONFLICT);
    }

    public static ClientException invalidQuizAnswer(Long questionId, Long answerId) {
        return new ClientException(
                String.format("Answer %d is not valid for question %d", answerId, questionId),
                "INVALID_QUIZ_ANSWER", HttpStatus.BAD_REQUEST);
    }

    public static ClientException quizNotAccessible(Long quizId, String reason) {
        return new ClientException(
                String.format("Quiz %d is not accessible: %s", quizId, reason),
                "QUIZ_NOT_ACCESSIBLE", HttpStatus.FORBIDDEN);
    }

    // ========== FILE & CONTENT ERRORS ==========
    public static ClientException fileUploadError(String message, String reason) {
        return new ClientException(
                String.format("File upload failed: %s", message),
                "FILE_UPLOAD_ERROR", HttpStatus.BAD_REQUEST);
    }

    public static ClientException fileSizeExceeded(long maxSize, long actualSize, String fileName) {
        return new ClientException(
                String.format("File '%s' size %d bytes exceeds maximum allowed size %d bytes", fileName, actualSize, maxSize),
                "FILE_SIZE_EXCEEDED", HttpStatus.PAYLOAD_TOO_LARGE);
    }

    public static ClientException unsupportedFileType(String fileType, List<String> supportedTypes, String fileName) {
        return new ClientException(
                String.format("File '%s' type '%s' not supported. Supported types: %s", fileName, fileType, supportedTypes),
                "UNSUPPORTED_FILE_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // ========== PAYMENT ERRORS ==========
    public static ClientException paymentFailed(String reason, String paymentId) {
        return new ClientException(
                String.format("Payment failed: %s", reason),
                "PAYMENT_FAILED", HttpStatus.PAYMENT_REQUIRED);
    }

    public static ClientException insufficientFunds(BigDecimal required, BigDecimal available) {
        return new ClientException(
                String.format("Insufficient funds. Required: %s, Available: %s", required, available),
                "INSUFFICIENT_FUNDS", HttpStatus.PAYMENT_REQUIRED);
    }

    // ========== PROGRESS & VALIDATION ERRORS ==========
    public static ClientException invalidProgress(Long moduleId, double progress) {
        return new ClientException(
                String.format("Invalid progress %f for module %d. Progress must be between 0 and 100", progress, moduleId),
                "INVALID_PROGRESS", HttpStatus.BAD_REQUEST);
    }

    public static ClientException prerequisiteNotMet(Long moduleId, List<Long> prerequisiteModules) {
        return new ClientException(
                String.format("Prerequisites not met for module %d. Required modules: %s", moduleId, prerequisiteModules),
                "PREREQUISITE_NOT_MET", HttpStatus.PRECONDITION_FAILED);
    }

    public static ClientException validationError(String field, Object value, String message) {
        return new ClientException(
                String.format("Validation failed for field '%s': %s", field, message),
                "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }
}