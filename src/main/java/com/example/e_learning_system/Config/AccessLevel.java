package com.example.e_learning_system.Config;

public enum AccessLevel {
    FULL("full"),
    PREVIEW("preview"),
    LIMITED("limited");

    private final String value;

    AccessLevel(String value) {
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