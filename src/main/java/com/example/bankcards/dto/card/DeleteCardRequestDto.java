package com.example.bankcards.dto.card;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public record DeleteCardRequestDto(
        @NotNull
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber
) {
}
