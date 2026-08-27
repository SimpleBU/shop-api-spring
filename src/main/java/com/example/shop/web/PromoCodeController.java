package com.example.shop.web;

import com.example.shop.dto.PromoCode;
import com.example.shop.dto.PromoCodeCreateRequest;
import com.example.shop.dto.RedemptionResult;
import com.example.shop.service.PromoCodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/promo-codes")
public class PromoCodeController extends AbstractCrudController<PromoCode> {

    private final PromoCodeService promoCodeService;

    public PromoCodeController(PromoCodeService promoCodeService) {
        this.promoCodeService = promoCodeService;
    }

    @Override
    protected List<PromoCode> loadAll() {
        return promoCodeService.findAll();
    }

    @Override
    protected PromoCode loadOne(String id) {
        return promoCodeService.findById(id);
    }

    @Override
    protected void removeOne(String id) {
        promoCodeService.delete(id);
    }

    @PostMapping
    public ResponseEntity<PromoCode> create(@Valid @RequestBody PromoCodeCreateRequest request) {
        PromoCode promoCode = promoCodeService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/promo-codes/" + promoCode.id())).body(promoCode);
    }

    @PostMapping("/{code}/redeem")
    public ResponseEntity<RedemptionResult> redeem(@PathVariable String code,
                                                   @RequestParam String orderId) {
        return ResponseEntity.ok(promoCodeService.redeem(code, orderId));
    }
}
