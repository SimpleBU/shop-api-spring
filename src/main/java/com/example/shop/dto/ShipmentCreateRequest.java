package com.example.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ShipmentCreateRequest(
        @NotBlank @Size(max = 40) String orderId,
        @NotBlank @Pattern(regexp = "^(CDEK|POCHTA|DHL|BOXBERRY)$") String carrier,
        @NotNull @Valid Address destination,
        @NotNull LocalDate estimatedDelivery) {
}
