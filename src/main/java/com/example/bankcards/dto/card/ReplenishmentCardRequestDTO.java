package com.example.bankcards.dto.card;

import java.math.BigDecimal;

public record ReplenishmentCardRequestDTO(
        String cardNumber,
        BigDecimal amount,
        String currency
) {
}
