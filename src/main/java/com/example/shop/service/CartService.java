package com.example.shop.service;

import com.example.shop.dto.Cart;
import com.example.shop.dto.CartItem;
import com.example.shop.dto.CartItemRequest;
import com.example.shop.dto.CartPatchRequest;
import com.example.shop.dto.Money;
import com.example.shop.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    private final Map<String, Cart> carts = new ConcurrentHashMap<>();

    public CartService() {
        carts.put("cart-1", new Cart("cart-1", "anna@example.com",
                List.of(new CartItem("SKU-10001", "Wireless headphones ANC", 1, Money.rub("12990.00"))),
                Money.rub("12990.00"), null, OffsetDateTime.now()));
        carts.put("cart-2", new Cart("cart-2", "boris@example.com", List.of(),
                new Money(BigDecimal.ZERO, "RUB"), null, OffsetDateTime.now()));
    }

    public Cart findById(String id) {
        Cart cart = carts.get(id);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", id);
        }
        return cart;
    }

    public Cart addItem(String id, CartItemRequest request) {
        Cart cart = findById(id);
        List<CartItem> items = new ArrayList<>(cart.items());
        int existing = indexOf(items, request.sku());
        if (existing >= 0) {
            CartItem current = items.get(existing);
            items.set(existing, current.withQuantity(current.quantity() + request.quantity()));
        } else {
            items.add(new CartItem(request.sku(), "Catalog item " + request.sku(), request.quantity(),
                    Money.rub("1990.00")));
        }
        return store(cart, items, cart.customerEmail(), cart.promoCode());
    }

    public Cart removeItem(String id, String sku) {
        Cart cart = findById(id);
        List<CartItem> items = new ArrayList<>(cart.items());
        int existing = indexOf(items, sku);
        if (existing < 0) {
            throw new ResourceNotFoundException("CartItem", sku);
        }
        items.remove(existing);
        return store(cart, items, cart.customerEmail(), cart.promoCode());
    }

    public Cart patch(String id, CartPatchRequest request) {
        Cart cart = findById(id);
        String email = request.customerEmail() == null ? cart.customerEmail() : request.customerEmail();
        String promo = request.promoCode() == null ? cart.promoCode() : request.promoCode();
        return store(cart, cart.items(), email, promo);
    }

    private Cart store(Cart cart, List<CartItem> items, String email, String promoCode) {
        Money subtotal = items.stream()
                .map(i -> i.unitPrice().multiply(i.quantity()))
                .reduce(Money::plus)
                .orElse(new Money(BigDecimal.ZERO, "RUB"));
        Cart updated = new Cart(cart.id(), email, List.copyOf(items), subtotal, promoCode, OffsetDateTime.now());
        carts.put(cart.id(), updated);
        return updated;
    }

    private int indexOf(List<CartItem> items, String sku) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).sku().equals(sku)) {
                return i;
            }
        }
        return -1;
    }

    public int size() {
        return carts.size();
    }
}
