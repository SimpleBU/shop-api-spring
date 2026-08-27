package com.example.shop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRoleUpdateRequest(
        @NotNull UserRole role,
        @Size(max = 200) String justification) {
}
