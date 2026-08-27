package com.example.shop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record Money(
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        @NotNull @Pattern(regexp = "^[A-Z]{3}$") String currency) {

    public static Money rub(String amount) {
        return new Money(new BigDecimal(amount), "RUB");
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount()), currency);
    }

    public Money multiply(int quantity) {
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
    }
}
