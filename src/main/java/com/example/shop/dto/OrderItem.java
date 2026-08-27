package com.example.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OrderItem(
        @NotBlank @Pattern(regexp = "^SKU-[0-9]{5}$") String sku,
        @NotBlank String title,
        @Min(1) @Max(999) int quantity,
        @NotNull @Valid Money unitPrice) {

    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }
}
