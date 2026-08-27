package com.example.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RefundRequest(
        @NotNull @Valid Money amount,
        @NotBlank @Size(max = 200) String reason) {
}
