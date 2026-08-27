package com.example.shop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderStatusUpdateRequest(
        @NotNull OrderStatus status,
        @Size(max = 300) String comment) {
}
