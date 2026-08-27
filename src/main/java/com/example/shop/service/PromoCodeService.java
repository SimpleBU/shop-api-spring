package com.example.shop.service;

import com.example.shop.dto.PromoCode;
import com.example.shop.dto.PromoCodeCreateRequest;
import com.example.shop.dto.RedemptionResult;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.SequenceGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PromoCodeService {

    private final Map<String, PromoCode> promoCodes = new ConcurrentHashMap<>();
    private final SequenceGenerator ids = new SequenceGenerator("promo", 40);

    public PromoCodeService() {
        seed("WELCOME10", 10, 5000);
        seed("BLACKFRIDAY", 30, 1000);
        seed("FREESHIP", 5, 100000);
    }

    private void seed(String code, int discount, int limit) {
        String id = ids.next();
        promoCodes.put(id, new PromoCode(id, code, discount, LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(6), limit, 0, true));
    }

    public List<PromoCode> findAll() {
        return promoCodes.values().stream()
                .sorted(Comparator.comparing(PromoCode::code))
                .toList();
    }

    public PromoCode findById(String id) {
        PromoCode promoCode = promoCodes.get(id);
        if (promoCode == null) {
            throw new ResourceNotFoundException("PromoCode", id);
        }
        return promoCode;
    }

    public PromoCode create(PromoCodeCreateRequest request) {
        String id = ids.next();
        PromoCode promoCode = new PromoCode(id, request.code(), request.discountPercent(), request.validFrom(),
                request.validTo(), request.usageLimit(), 0, true);
        promoCodes.put(id, promoCode);
        return promoCode;
    }

    public void delete(String id) {
        if (promoCodes.remove(id) == null) {
            throw new ResourceNotFoundException("PromoCode", id);
        }
    }

    public RedemptionResult redeem(String code, String orderId) {
        Optional<PromoCode> found = promoCodes.values().stream()
                .filter(p -> p.code().equalsIgnoreCase(code))
                .findFirst();
        if (found.isEmpty()) {
            return new RedemptionResult(code, false, 0, 0, "Promo code is unknown");
        }
        PromoCode promoCode = found.get();
        if (!promoCode.active() || promoCode.usedCount() >= promoCode.usageLimit()) {
            return new RedemptionResult(code, false, 0, 0, "Promo code is exhausted");
        }
        PromoCode updated = promoCode.withUsedCount(promoCode.usedCount() + 1);
        promoCodes.put(updated.id(), updated);
        return new RedemptionResult(code, true, updated.discountPercent(),
                updated.usageLimit() - updated.usedCount(), "Applied to order " + orderId);
    }

    public int size() {
        return promoCodes.size();
    }
}
