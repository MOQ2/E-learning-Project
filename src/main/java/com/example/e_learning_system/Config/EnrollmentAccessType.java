package com.example.e_learning_system.Config;

public enum EnrollmentAccessType {
    FREE("free"),
    SUBSCRIPTION("subscription"),
    ONE_TIME_PURCHASE("one_time_purchase"),
    ADMIN_GRANTED("admin_granted");

    private final String value;

    EnrollmentAccessType(String value) {
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