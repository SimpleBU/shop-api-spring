package com.example.shop.service;

import com.example.shop.dto.Money;
import com.example.shop.dto.Order;
import com.example.shop.dto.OrderCreateRequest;
import com.example.shop.dto.OrderItem;
import com.example.shop.dto.OrderStatus;
import com.example.shop.dto.OrderSummary;
import com.example.shop.dto.Address;
import com.example.shop.exception.BusinessRuleException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.SequenceGenerator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final SequenceGenerator ids = new SequenceGenerator("ord", 5000);

    public OrderService() {
        Address moscow = new Address("RU", "Moscow", "Tverskaya 1", "125009");
        Address kazan = new Address("RU", "Kazan", "Bauman 5", "420111");
        seed(OrderStatus.PAID, "anna@example.com", moscow,
                new OrderItem("SKU-10001", "Wireless headphones ANC", 1, Money.rub("12990.00")));
        seed(OrderStatus.SHIPPED, "boris@example.com", kazan,
                new OrderItem("SKU-10003", "Clean Architecture", 2, Money.rub("2190.00")));
        seed(OrderStatus.NEW, "clara@example.com", moscow,
                new OrderItem("SKU-10005", "Arabica beans 1kg", 3, Money.rub("1890.00")));
    }

    private void seed(OrderStatus status, String email, Address address, OrderItem item) {
        String id = ids.next();
        orders.put(id, new Order(id, email, status, List.of(item), item.lineTotal(), address, null,
                OffsetDateTime.now().minusDays(orders.size() + 1L), OffsetDateTime.now()));
    }

    public List<Order> findAll(OrderStatus status, String customerEmail) {
        return orders.values().stream()
                .filter(o -> status == null || o.status() == status)
                .filter(o -> customerEmail == null || o.customerEmail().equalsIgnoreCase(customerEmail))
                .sorted(Comparator.comparing(Order::id))
                .toList();
    }

    public Order findById(String id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new ResourceNotFoundException("Order", id);
        }
        return order;
    }

    public Order create(OrderCreateRequest request) {
        String id = ids.next();
        Money total = request.items().stream()
                .map(OrderItem::lineTotal)
                .reduce(Money::plus)
                .orElse(new Money(BigDecimal.ZERO, "RUB"));
        Order order = new Order(id, request.customerEmail(), OrderStatus.NEW, List.copyOf(request.items()),
                total, request.shippingAddress(), request.promoCode(), OffsetDateTime.now(),
                OffsetDateTime.now());
        orders.put(id, order);
        return order;
    }

    public Order changeStatus(String id, OrderStatus status) {
        Order order = findById(id);
        if (order.status() == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("ORDER_CANCELLED", "Cancelled order " + id + " cannot change status");
        }
        Order updated = order.withStatus(status);
        orders.put(id, updated);
        return updated;
    }

    public void delete(String id) {
        if (orders.remove(id) == null) {
            throw new ResourceNotFoundException("Order", id);
        }
    }

    public OrderSummary summary(LocalDate from, LocalDate to) {
        Map<OrderStatus, Long> byStatus = new EnumMap<>(OrderStatus.class);
        orders.values().forEach(o -> byStatus.merge(o.status(), 1L, Long::sum));
        Money revenue = orders.values().stream()
                .map(Order::total)
                .reduce(Money::plus)
                .orElse(new Money(BigDecimal.ZERO, "RUB"));
        return new OrderSummary(from, to, orders.size(), revenue, byStatus);
    }

    public byte[] renderInvoicePdf(String id) {
        Order order = findById(id);
        String body = "%PDF-1.4\n% invoice for order " + order.id()
                + " total " + order.total().amount() + " " + order.total().currency() + "\n%%EOF\n";
        return body.getBytes(StandardCharsets.ISO_8859_1);
    }

    public int size() {
        return orders.size();
    }
}
