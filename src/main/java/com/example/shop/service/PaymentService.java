package com.example.shop.service;

import com.example.shop.dto.CardPaymentMethod;
import com.example.shop.dto.Money;
import com.example.shop.dto.Payment;
import com.example.shop.dto.PaymentRequest;
import com.example.shop.dto.PaymentStatus;
import com.example.shop.dto.RefundRequest;
import com.example.shop.dto.WalletPaymentMethod;
import com.example.shop.exception.BusinessRuleException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.SequenceGenerator;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentService {

    private final Map<String, Payment> payments = new ConcurrentHashMap<>();
    private final SequenceGenerator ids = new SequenceGenerator("pay", 700);

    public PaymentService() {
        String first = ids.next();
        payments.put(first, new Payment(first, "ord-5001", PaymentStatus.CAPTURED, Money.rub("12990.00"),
                new CardPaymentMethod("4242", "12/27", "VISA"), "acq-ref-90001", OffsetDateTime.now()));
        String second = ids.next();
        payments.put(second, new Payment(second, "ord-5002", PaymentStatus.AUTHORIZED, Money.rub("4380.00"),
                new WalletPaymentMethod("yoomoney", "wallet-88213"), "acq-ref-90002", OffsetDateTime.now()));
    }

    public List<Payment> findAll(PaymentStatus status, String orderId) {
        return payments.values().stream()
                .filter(p -> status == null || p.status() == status)
                .filter(p -> orderId == null || p.orderId().equals(orderId))
                .sorted(Comparator.comparing(Payment::id))
                .toList();
    }

    public Payment findById(String id) {
        Payment payment = payments.get(id);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment", id);
        }
        return payment;
    }

    public Payment create(PaymentRequest request) {
        String id = ids.next();
        Payment payment = new Payment(id, request.orderId(), PaymentStatus.AUTHORIZED, request.amount(),
                request.method(), "acq-ref-" + id, OffsetDateTime.now());
        payments.put(id, payment);
        return payment;
    }

    public Payment refund(String id, RefundRequest request) {
        Payment payment = findById(id);
        if (payment.status() != PaymentStatus.CAPTURED && payment.status() != PaymentStatus.AUTHORIZED) {
            throw new BusinessRuleException("REFUND_NOT_ALLOWED",
                    "Payment " + id + " in status " + payment.status() + " cannot be refunded");
        }
        if (request.amount().amount().compareTo(payment.amount().amount()) > 0) {
            throw new BusinessRuleException("REFUND_TOO_LARGE", "Refund exceeds captured amount");
        }
        Payment refunded = payment.withStatus(PaymentStatus.REFUNDED);
        payments.put(id, refunded);
        return refunded;
    }

    public Optional<Payment> applyProviderCallback(String providerReference, PaymentStatus status) {
        return payments.values().stream()
                .filter(p -> p.providerReference().equals(providerReference))
                .findFirst()
                .map(p -> {
                    Payment updated = p.withStatus(status);
                    payments.put(p.id(), updated);
                    return updated;
                });
    }

    public int size() {
        return payments.size();
    }
}
