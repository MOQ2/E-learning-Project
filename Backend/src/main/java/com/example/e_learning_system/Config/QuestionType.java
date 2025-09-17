package com.example.e_learning_system.Config;

public enum QuestionType {
    MULTIPLE_CHOICE("multiple_choice"),
    TRUE_FALSE("true_false"),
    SHORT_ANSWER("short_answer");

    private final String value;

    QuestionType(String value) {
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
