package com.example.shop.dto;

import java.time.OffsetDateTime;

public record Payment(
        String id,
        String orderId,
        PaymentStatus status,
        Money amount,
        PaymentMethod method,
        String providerReference,
        OffsetDateTime createdAt) {

    public Payment withStatus(PaymentStatus newStatus) {
        return new Payment(id, orderId, newStatus, amount, method, providerReference, createdAt);
    }
}
