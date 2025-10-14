package com.example.bankcards.entity.enums;

public enum Currency {
    RUB, USD, EUR;

    public static Currency fromString(String currencyString) {
        for (Currency currency : Currency.values()) {
            if(currency.toString().equals(currencyString)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Invalid card status: " + currencyString);
    }
}
