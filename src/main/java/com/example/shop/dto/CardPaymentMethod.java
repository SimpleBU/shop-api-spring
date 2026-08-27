package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CardPaymentMethod(
        @NotBlank @Pattern(regexp = "^[0-9]{4}$") String last4,
        @NotBlank @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$") String expiry,
        @NotBlank @Pattern(regexp = "^(VISA|MASTERCARD|MIR)$") String brand) implements PaymentMethod {

    @Override
    public String kind() {
        return "CARD";
    }
}
