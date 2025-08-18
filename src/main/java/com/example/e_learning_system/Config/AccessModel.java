package com.example.e_learning_system.Config;

public enum AccessModel {
    FREE("free"),
    ONE_TIME("one_time"),
    SUBSCRIPTION("subscription");

    private final String value;

    AccessModel(String value) {
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