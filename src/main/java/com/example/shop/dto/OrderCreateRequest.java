package com.example.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateRequest(
        @NotNull @Email @Size(max = 160) String customerEmail,
        @NotEmpty @Size(max = 50) List<@Valid OrderItem> items,
        @NotNull @Valid Address shippingAddress,
        @Pattern(regexp = "^[A-Z0-9]{4,16}$") String promoCode) {
}
