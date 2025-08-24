package com.example.e_learning_system.Config;

public enum BillingCycle {
    MONTHLY("monthly"),
    QUARTERLY("quarterly"),
    ANNUAL("annual"),
    LIFETIME("lifetime");

    private final String value;

    BillingCycle(String value) {
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
