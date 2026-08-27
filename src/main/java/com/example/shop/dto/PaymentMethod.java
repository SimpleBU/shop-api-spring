package com.example.shop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payment instrument used to settle an order.
 *
 * <p>A single flat record instead of a type hierarchy: {@code kind} selects which group of
 * fields is meaningful. For {@code CARD} that is {@code last4}, {@code expiry} and
 * {@code brand}; for {@code WALLET} it is {@code provider} and {@code walletId}. The fields
 * of the other group stay null.
 */
public record PaymentMethod(
        @NotNull PaymentMethodKind kind,
        @Pattern(regexp = "^[0-9]{4}$") String last4,
        @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$") String expiry,
        @Pattern(regexp = "^(VISA|MASTERCARD|MIR)$") String brand,
        @Size(max = 40) String provider,
        @Size(max = 64) String walletId) {
}
