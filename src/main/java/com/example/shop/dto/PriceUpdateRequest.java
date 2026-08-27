package com.example.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PriceUpdateRequest(
        @NotNull @Valid Money price,
        @Size(max = 200) String reason) {
}
