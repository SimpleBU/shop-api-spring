package com.example.shop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ModerationRequest(
        @NotNull Boolean published,
        @Size(max = 500) String note) {
}
