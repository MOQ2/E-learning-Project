package com.example.e_learning_system.Config;

public enum EnrollmentStatus {
    ENROLLED("enrolled"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    DROPPED("dropped"),
    SUSPENDED("suspended");

    private final String value;

    EnrollmentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}