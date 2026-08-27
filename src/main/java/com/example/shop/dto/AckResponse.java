package com.example.shop.dto;

import java.time.OffsetDateTime;

public record AckResponse(
        String status,
        String reference,
        OffsetDateTime acknowledgedAt) {
}
