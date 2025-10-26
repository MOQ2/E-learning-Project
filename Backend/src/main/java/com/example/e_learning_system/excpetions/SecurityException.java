package com.example.e_learning_system.excpetions;

import org.springframework.http.HttpStatus;

public class SecurityException extends BaseException {

    public SecurityException(String message, String errorCode, HttpStatus status) {
        super(message, errorCode, status);
    }

    public SecurityException(String message, String errorCode, HttpStatus status, Object[] arguments) {
        super(message, errorCode, status, arguments);
    }

    public SecurityException(String message, String errorCode, HttpStatus status, Throwable cause, Object[] arguments) {
        super(message, errorCode, status, cause, arguments);
    }

    // Factory methods for security-related errors
    public static SecurityException tokenExpired() {
        return new SecurityException("Authentication token has expired", "TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED);
    }

    public static SecurityException tokenInvalid() {
        return new SecurityException("Invalid authentication token", "TOKEN_INVALID", HttpStatus.UNAUTHORIZED);
    }

    public static SecurityException tokenMissing() {
        return new SecurityException("Authentication token is missing", "TOKEN_MISSING", HttpStatus.UNAUTHORIZED);
    }

    public static SecurityException accessDenied(String resource) {
        return new SecurityException("Access denied to resource: " + resource, "ACCESS_DENIED", HttpStatus.FORBIDDEN);
    }

    public static SecurityException roleNotAuthorized(String role, String action) {
        return new SecurityException("Role '" + role + "' is not authorized to perform: " + action, "ROLE_NOT_AUTHORIZED", HttpStatus.FORBIDDEN);
    }

    public static SecurityException bruteForceDetected(String username) {
        return new SecurityException("Brute force attack detected for user: " + username, "BRUTE_FORCE_DETECTED", HttpStatus.TOO_MANY_REQUESTS);
    }

    public static SecurityException suspiciousActivity(String details) {
        return new SecurityException("Suspicious activity detected: " + details, "SUSPICIOUS_ACTIVITY", HttpStatus.FORBIDDEN);
    }

    public static SecurityException passwordPolicyViolation(String requirement) {
        return new SecurityException("Password does not meet requirement: " + requirement, "PASSWORD_POLICY_VIOLATION", HttpStatus.BAD_REQUEST);
    }

    public static SecurityException twoFactorRequired() {
        return new SecurityException("Two-factor authentication is required", "TWO_FACTOR_REQUIRED", HttpStatus.FORBIDDEN);
    }
}
