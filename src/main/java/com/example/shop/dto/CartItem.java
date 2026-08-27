package com.example.shop.dto;

public record CartItem(
        String sku,
        String title,
        int quantity,
        Money unitPrice) {

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(sku, title, newQuantity, unitPrice);
    }
}
