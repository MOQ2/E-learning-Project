package com.example.e_learning_system.Config;
public enum CourseStatus {
    DRAFT("draft"),
    PUBLISHED("published"),
    ARCHIVED("archived");
    
    private final String value;

    CourseStatus(String value) {
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