package com.example.e_learning_system.excpetions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@Getter
@Setter
public class ValidationException extends BaseException {
    private List<String> validationErrorsMessages;

    public ValidationException(String message, List<String> validationErrorsMessages) {
        super(message, "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
        this.validationErrorsMessages = validationErrorsMessages != null ? validationErrorsMessages : new ArrayList<>();
    }

    // Factory methods for common validation scenarios
    public static ValidationException formValidation(List<String> errors) {
        return new ValidationException("Form validation failed", errors);
    }

    public static ValidationException formValidation(String... errors) {
        return new ValidationException("Form validation failed", Arrays.asList(errors));
    }

    public static ValidationException requiredFields(List<String> missingFields) {
        List<String> errors = missingFields.stream()
                .map(field -> field + " is required")
                .toList();
        return new ValidationException("Required fields missing", errors);
    }

    public static ValidationException requiredFields(String... missingFields) {
        return requiredFields(Arrays.asList(missingFields));
    }

    public static ValidationException invalidFormat(String field, String expectedFormat) {
        return new ValidationException(
                "Invalid format validation",
                List.of(field + " must be in format: " + expectedFormat)
        );
    }

    public static ValidationException rangeValidation(String field, Object min, Object max, Object actual) {
        return new ValidationException(
                "Range validation failed",
                List.of(String.format("%s must be between %s and %s, but was %s", field, min, max, actual))
        );
    }

    public static ValidationException lengthValidation(String field, int minLength, int maxLength, int actualLength) {
        return new ValidationException(
                "Length validation failed",
                List.of(String.format("%s length must be between %d and %d characters, but was %d",
                        field, minLength, maxLength, actualLength))
        );
    }

    public static ValidationException emailValidation(String email) {
        return new ValidationException(
                "Email validation failed",
                List.of("Invalid email format: " + email)
        );
    }

    public static ValidationException passwordStrength(List<String> requirements) {
        List<String> errors = requirements.stream()
                .map(req -> "Password must " + req)
                .toList();
        return new ValidationException("Password strength validation failed", errors);
    }

    // Method to add more validation errors
    public ValidationException addError(String error) {
        this.validationErrorsMessages.add(error);
        return this;
    }

    public ValidationException addErrors(List<String> errors) {
        this.validationErrorsMessages.addAll(errors);
        return this;
    }

    // Check if has errors
    public boolean hasErrors() {
        return validationErrorsMessages != null && !validationErrorsMessages.isEmpty();
    }

    public int getErrorCount() {
        return validationErrorsMessages != null ? validationErrorsMessages.size() : 0;
    }
}