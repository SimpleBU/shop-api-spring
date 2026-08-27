package com.example.shop.web;

import com.example.shop.dto.Cart;
import com.example.shop.dto.CartItemRequest;
import com.example.shop.dto.CartPatchRequest;
import com.example.shop.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping({"/carts/{id}", "/baskets/{id}"})
    @ResponseBody
    public ResponseEntity<Cart> getCart(@PathVariable String id) {
        return ResponseEntity.ok(cartService.findById(id));
    }

    @PostMapping("/carts/{id}/items")
    @ResponseBody
    public ResponseEntity<Cart> addItem(@PathVariable String id,
                                        @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(id, request));
    }

    @DeleteMapping("/carts/{id}/items/{sku}")
    @ResponseBody
    public ResponseEntity<Cart> removeItem(@PathVariable String id, @PathVariable String sku) {
        return ResponseEntity.ok(cartService.removeItem(id, sku));
    }

    @PatchMapping("/carts/{id}")
    @ResponseBody
    public ResponseEntity<Cart> patchCart(@PathVariable String id,
                                          @Valid @RequestBody CartPatchRequest request) {
        return ResponseEntity.ok(cartService.patch(id, request));
    }
}
