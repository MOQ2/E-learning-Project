package com.example.e_learning_system.Config;

public enum Currency {
    USD("USD"),
    EUR("EUR"),
    GOD("GOD"),
    ILS("ILS");

    private final String value;

    Currency(String value) {
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
