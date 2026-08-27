package com.example.shop.web;

import com.example.shop.dto.PageResponse;
import com.example.shop.dto.Payment;
import com.example.shop.dto.PaymentRequest;
import com.example.shop.dto.PaymentStatus;
import com.example.shop.dto.RefundRequest;
import com.example.shop.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/api/v1/payments")
    public ResponseEntity<PageResponse<Payment>> list(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String orderId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Payment> found = paymentService.findAll(status, orderId);
        return ResponseEntity.ok(PageResponse.of(found, page, size));
    }

    @GetMapping("/api/v1/payments/{id}")
    public ResponseEntity<Payment> getOne(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @PostMapping("/api/v1/payments")
    public ResponseEntity<Payment> authorize(@Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/payments/" + payment.id())).body(payment);
    }

    @PostMapping("/api/v1/payments/{id}/refund")
    public ResponseEntity<Payment> refund(@PathVariable String id,
                                          @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.refund(id, request));
    }

    @RequestMapping(value = "/api/v1/payments", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> describe() {
        return ResponseEntity.ok()
                .header(HttpHeaders.ALLOW, "GET,POST,OPTIONS")
                .header("X-Payment-Methods", "CARD,WALLET")
                .build();
    }
}
