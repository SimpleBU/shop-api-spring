package com.example.shop.web;

import com.example.shop.dto.AckResponse;
import com.example.shop.dto.CacheStats;
import com.example.shop.dto.PaymentCallbackRequest;
import com.example.shop.service.OpsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternalOpsController {

    private final OpsService opsService;

    public InternalOpsController(OpsService opsService) {
        this.opsService = opsService;
    }

    @GetMapping("/internal/debug/cache")
    public ResponseEntity<CacheStats> cacheStats() {
        return ResponseEntity.ok(opsService.cacheStats());
    }

    @PostMapping("/internal/webhooks/payment-callback")
    public ResponseEntity<AckResponse> paymentCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        return ResponseEntity.accepted().body(opsService.acceptPaymentCallback(request));
    }
}
