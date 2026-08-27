package com.example.shop.dto;

import java.time.OffsetDateTime;

public record Review(
        String id,
        String productId,
        String authorEmail,
        int rating,
        String text,
        boolean published,
        String moderationNote,
        OffsetDateTime createdAt) {

    public Review withModeration(boolean newPublished, String note) {
        return new Review(id, productId, authorEmail, rating, text, newPublished, note, createdAt);
    }
}
