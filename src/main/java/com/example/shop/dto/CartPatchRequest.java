package com.example.shop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CartPatchRequest(
        @Email @Size(max = 160) String customerEmail,
        @Pattern(regexp = "^[A-Z0-9]{4,16}$") String promoCode) {
}
