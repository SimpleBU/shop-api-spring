package com.example.shop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotBlank @Size(max = 40) String productId,
        @NotNull @Email @Size(max = 160) String authorEmail,
        @Min(1) @Max(5) int rating,
        @NotBlank @Size(min = 10, max = 4000) String text) {
}
