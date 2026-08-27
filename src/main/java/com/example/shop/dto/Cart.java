package com.example.shop.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record Cart(
        String id,
        String customerEmail,
        List<CartItem> items,
        Money subtotal,
        String promoCode,
        OffsetDateTime updatedAt) {
}
