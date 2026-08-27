package com.example.shop.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record Order(
        String id,
        String customerEmail,
        OrderStatus status,
        List<OrderItem> items,
        Money total,
        Address shippingAddress,
        String promoCode,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, customerEmail, newStatus, items, total, shippingAddress, promoCode,
                createdAt, OffsetDateTime.now());
    }
}
