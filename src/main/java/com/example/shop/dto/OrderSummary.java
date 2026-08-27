package com.example.shop.dto;

import java.time.LocalDate;
import java.util.Map;

public record OrderSummary(
        LocalDate from,
        LocalDate to,
        long ordersCount,
        Money revenue,
        Map<OrderStatus, Long> byStatus) {
}
