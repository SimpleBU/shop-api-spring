package com.example.shop.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record Product(
        String id,
        String sku,
        String title,
        String description,
        ProductCategory category,
        Money price,
        int stock,
        List<String> tags,
        OffsetDateTime updatedAt) {

    public Product withPrice(Money newPrice) {
        return new Product(id, sku, title, description, category, newPrice, stock, tags, OffsetDateTime.now());
    }

    public Product withStock(int newStock) {
        return new Product(id, sku, title, description, category, price, newStock, tags, OffsetDateTime.now());
    }
}
