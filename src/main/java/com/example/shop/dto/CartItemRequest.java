package com.example.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CartItemRequest(
        @NotBlank @Pattern(regexp = "^SKU-[0-9]{5}$") String sku,
        @Min(1) @Max(99) int quantity) {
}
