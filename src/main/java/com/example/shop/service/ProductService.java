package com.example.shop.service;

import com.example.shop.dto.Money;
import com.example.shop.dto.Product;
import com.example.shop.dto.ProductCategory;
import com.example.shop.dto.ProductRequest;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.SequenceGenerator;
import com.example.shop.model.StockReservation;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ProductService {

    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final List<StockReservation> reservations = new CopyOnWriteArrayList<>();
    private final SequenceGenerator ids = new SequenceGenerator("prd", 1000);
    private volatile List<String> snapshot = List.of();

    public ProductService() {
        seed("SKU-10001", "Wireless headphones ANC", ProductCategory.ELECTRONICS, "12990.00", 42,
                List.of("audio", "bluetooth"));
        seed("SKU-10002", "Espresso machine Base", ProductCategory.HOME, "24500.00", 11,
                List.of("kitchen"));
        seed("SKU-10003", "Clean Architecture", ProductCategory.BOOKS, "2190.00", 130,
                List.of("software", "paperback"));
        seed("SKU-10004", "Merino wool sweater", ProductCategory.CLOTHING, "7450.00", 27,
                List.of("winter"));
        seed("SKU-10005", "Arabica beans 1kg", ProductCategory.GROCERY, "1890.00", 300,
                List.of("coffee"));
        refreshSnapshot();
    }

    private void seed(String sku, String title, ProductCategory category, String price, int stock,
                      List<String> tags) {
        String id = ids.next();
        products.put(id, new Product(id, sku, title, title + " - stock item", category, Money.rub(price),
                stock, tags, OffsetDateTime.now()));
    }

    public List<Product> findAll(ProductCategory category, String query) {
        return products.values().stream()
                .filter(p -> category == null || p.category() == category)
                .filter(p -> query == null || p.title().toLowerCase().contains(query.toLowerCase()))
                .sorted(Comparator.comparing(Product::sku))
                .toList();
    }

    public Product findById(String id) {
        Product product = products.get(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product", id);
        }
        return product;
    }

    public Optional<Product> findOptional(String id) {
        return Optional.ofNullable(products.get(id));
    }

    public Product create(ProductRequest request) {
        String id = ids.next();
        Product product = new Product(id, request.sku(), request.title(), request.description(),
                request.category(), request.price(), request.stock(),
                request.tags() == null ? List.of() : List.copyOf(request.tags()), OffsetDateTime.now());
        products.put(id, product);
        return product;
    }

    public Product replace(String id, ProductRequest request) {
        findById(id);
        Product product = new Product(id, request.sku(), request.title(), request.description(),
                request.category(), request.price(), request.stock(),
                request.tags() == null ? List.of() : List.copyOf(request.tags()), OffsetDateTime.now());
        products.put(id, product);
        return product;
    }

    public Product updatePrice(String id, Money price) {
        Product updated = findById(id).withPrice(price);
        products.put(id, updated);
        return updated;
    }

    public void delete(String id) {
        if (products.remove(id) == null) {
            throw new ResourceNotFoundException("Product", id);
        }
    }

    public boolean isAvailable(String id) {
        return findOptional(id).map(p -> p.stock() > 0).orElse(false);
    }

    public int refreshSnapshot() {
        List<String> skus = new ArrayList<>(products.values().stream().map(Product::sku).sorted().toList());
        this.snapshot = List.copyOf(skus);
        return snapshot.size();
    }

    public List<String> snapshot() {
        return snapshot;
    }

    public int releaseExpiredReservations() {
        Instant now = Instant.now();
        List<StockReservation> expired = reservations.stream().filter(r -> r.isExpired(now)).toList();
        reservations.removeAll(expired);
        return expired.size();
    }

    public int catalogSize() {
        return products.size();
    }
}
