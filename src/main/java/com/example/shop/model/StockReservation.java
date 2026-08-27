package com.example.shop.model;

import java.time.Instant;

public record StockReservation(String sku, int quantity, Instant expiresAt) {

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }
}
