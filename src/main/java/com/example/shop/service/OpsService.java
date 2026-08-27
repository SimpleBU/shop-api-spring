package com.example.shop.service;

import com.example.shop.dto.AckResponse;
import com.example.shop.dto.CacheStats;
import com.example.shop.dto.PaymentCallbackRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OpsService {

    private final ProductService productService;
    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentService paymentService;
    private final AtomicLong hits = new AtomicLong(15321);
    private final AtomicLong misses = new AtomicLong(842);

    public OpsService(ProductService productService, OrderService orderService, CartService cartService,
                      PaymentService paymentService) {
        this.productService = productService;
        this.orderService = orderService;
        this.cartService = cartService;
        this.paymentService = paymentService;
    }

    public CacheStats cacheStats() {
        Map<String, Integer> regions = new LinkedHashMap<>();
        regions.put("products", productService.catalogSize());
        regions.put("orders", orderService.size());
        regions.put("carts", cartService.size());
        regions.put("payments", paymentService.size());
        regions.put("catalog-snapshot", productService.snapshot().size());
        hits.incrementAndGet();
        return new CacheStats(regions, hits.get(), misses.get(), OffsetDateTime.now());
    }

    public AckResponse acceptPaymentCallback(PaymentCallbackRequest request) {
        String status = paymentService
                .applyProviderCallback(request.providerReference(), request.status())
                .map(p -> "APPLIED")
                .orElse("IGNORED");
        misses.incrementAndGet();
        return new AckResponse(status, request.providerReference(), OffsetDateTime.now());
    }
}
