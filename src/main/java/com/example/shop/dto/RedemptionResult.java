package com.example.shop.dto;

public record RedemptionResult(
        String code,
        boolean accepted,
        int discountPercent,
        int remainingUses,
        String message) {
}
