package com.example.shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record PromoCodeCreateRequest(
        @NotBlank @Pattern(regexp = "^[A-Z0-9]{4,16}$") String code,
        @Min(1) @Max(90) int discountPercent,
        @NotNull LocalDate validFrom,
        @NotNull LocalDate validTo,
        @Min(1) @Max(100000) int usageLimit) {
}
