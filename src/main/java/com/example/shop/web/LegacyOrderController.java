package com.example.shop.web;

import com.example.shop.dto.Order;
import com.example.shop.dto.OrderCreateRequest;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LegacyOrderController {

    private final OrderService orderService;

    public LegacyOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @RequestMapping(value = "/legacy/orders", method = RequestMethod.POST)
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderService.create(request);
        return ResponseEntity.ok()
                .header("X-Legacy-Endpoint", "true")
                .body(order);
    }

    @GetMapping("/api/v0/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        return ResponseEntity.ok()
                .header("X-Legacy-Endpoint", "true")
                .body(orderService.findById(id));
    }
}
