package com.example.e_learning_system.Config;

public enum Currency {
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    JPY("JPY"),
    AUD("AUD"),
    CAD("CAD"),
    CHF("CHF"),
    CNY("CNY"),
    SEK("SEK"),
    NZD("NZD"),
    MXN("MXN"),
    SGD("SGD"),
    HKD("HKD"),
    NOK("NOK"),
    KRW("KRW"),
    TRY("TRY"),
    RUB("RUB"),
    INR("INR"),
    BRL("BRL"),
    ZAR("ZAR"),
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
