package com.example.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Address(
        @NotBlank @Size(max = 2) @Pattern(regexp = "^[A-Z]{2}$") String country,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Size(max = 160) String street,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$") String postalCode) {
}
