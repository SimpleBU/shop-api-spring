package com.example.shop.web;

import com.example.shop.dto.Order;
import com.example.shop.dto.OrderCreateRequest;
import com.example.shop.dto.OrderStatus;
import com.example.shop.dto.OrderStatusUpdateRequest;
import com.example.shop.dto.OrderSummary;
import com.example.shop.dto.PageResponse;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<Order>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String customerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Order> found = orderService.findAll(status, customerEmail);
        return ResponseEntity.ok(PageResponse.of(found, page, size));
    }

    @GetMapping(value = "/summary", headers = "X-Api-Version=2")
    public ResponseEntity<OrderSummary> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(orderService.summary(from, to));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOne(@PathVariable String id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Order> create(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + order.id())).body(order);
    }

    @RequestMapping(value = "/{id}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<Order> changeStatus(@PathVariable String id,
                                              @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.changeStatus(id, request.status()));
    }

    @RequestMapping(value = "/{id}/export", method = RequestMethod.GET,
            params = "action=export", produces = "application/pdf")
    public ResponseEntity<byte[]> export(@PathVariable String id,
                                         @RequestParam("action") String action) {
        byte[] pdf = orderService.renderInvoicePdf(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"order-" + id + ".pdf\"")
                .header("X-Export-Action", action)
                .body(pdf);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
