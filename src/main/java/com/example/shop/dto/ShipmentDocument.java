package com.example.shop.dto;

import java.time.OffsetDateTime;

public record ShipmentDocument(
        String id,
        String kind,
        String fileName,
        long sizeBytes,
        OffsetDateTime uploadedAt) {
}
