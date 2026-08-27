package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentCallbackRequest(
        @NotBlank String providerReference,
        @NotNull PaymentStatus status,
        @NotBlank String signature) {
}
