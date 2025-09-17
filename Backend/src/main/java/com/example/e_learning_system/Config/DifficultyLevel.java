package com.example.e_learning_system.Config;

public enum DifficultyLevel {
    BIGINNER("biginner"),
    INTERMEDIATE("intermediate"),
    ADVANCED("advanced");

    private final String value;

    DifficultyLevel(String value) {
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