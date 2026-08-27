package com.example.shop.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        String path,
        OffsetDateTime timestamp,
        List<String> details) {
}
