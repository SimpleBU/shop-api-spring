package com.example.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductRequest(
        @NotBlank @Pattern(regexp = "^SKU-[0-9]{5}$") String sku,
        @NotBlank @Size(min = 3, max = 120) String title,
        @Size(max = 2000) String description,
        @NotNull ProductCategory category,
        @NotNull @Valid Money price,
        @Min(0) int stock,
        @Size(max = 10) List<@Size(max = 24) String> tags) {
}
