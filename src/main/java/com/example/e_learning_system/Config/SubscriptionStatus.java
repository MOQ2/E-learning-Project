package com.example.e_learning_system.Config;

public enum SubscriptionStatus {
    ACTIVE("active"),
    CANCELLED("cancelled"),
    EXPIRED("expired"),
    SUSPENDED("suspended"),
    PENDING("pending");

    private final String value;

    SubscriptionStatus(String value) {
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
