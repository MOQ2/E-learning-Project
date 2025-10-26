package com.example.e_learning_system.Config;

public enum PaymentType {
    SUBSCRIPTION("subscription"),
    COURSE_PURCHASE("course_purchase"),
    REFUND("refund");

    private final String value;

    PaymentType(String value) {
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