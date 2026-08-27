package com.example.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PaymentRequest(
        @NotBlank @Size(max = 40) String orderId,
        @NotNull @Valid Money amount,
        @NotNull @Valid PaymentMethod method,
        @Size(max = 120) String idempotencyKey) {
}
