package com.example.shop.dto;

import java.time.OffsetDateTime;

public record UserDto(
        String id,
        String email,
        String displayName,
        UserRole role,
        boolean blocked,
        OffsetDateTime registeredAt) {

    public UserDto withRole(UserRole newRole) {
        return new UserDto(id, email, displayName, newRole, blocked, registeredAt);
    }
}
