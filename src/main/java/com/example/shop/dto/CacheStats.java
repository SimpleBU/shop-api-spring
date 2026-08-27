package com.example.shop.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record CacheStats(
        Map<String, Integer> entriesByRegion,
        long hits,
        long misses,
        OffsetDateTime collectedAt) {
}
