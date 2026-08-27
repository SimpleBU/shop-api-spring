package com.example.shop.dto;

import java.time.LocalDate;

public record PromoCode(
        String id,
        String code,
        int discountPercent,
        LocalDate validFrom,
        LocalDate validTo,
        int usageLimit,
        int usedCount,
        boolean active) {

    public PromoCode withUsedCount(int newUsedCount) {
        return new PromoCode(id, code, discountPercent, validFrom, validTo, usageLimit, newUsedCount, active);
    }
}
