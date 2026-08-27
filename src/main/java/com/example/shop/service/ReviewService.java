package com.example.shop.service;

import com.example.shop.dto.ModerationRequest;
import com.example.shop.dto.Review;
import com.example.shop.dto.ReviewCreateRequest;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.SequenceGenerator;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReviewService {

    private final Map<String, Review> reviews = new ConcurrentHashMap<>();
    private final SequenceGenerator ids = new SequenceGenerator("rev", 100);

    public ReviewService() {
        seed("prd-1001", "anna@example.com", 5, "Great noise cancelling, battery lasts a full week.");
        seed("prd-1003", "boris@example.com", 4, "Solid classic, some chapters feel repetitive though.");
        seed("prd-1005", "clara@example.com", 3, "Beans are fresh but the roast is darker than described.");
    }

    private void seed(String productId, String email, int rating, String text) {
        String id = ids.next();
        reviews.put(id, new Review(id, productId, email, rating, text, true, null, OffsetDateTime.now()));
    }

    public List<Review> findAll(String productId, Integer minRating) {
        return reviews.values().stream()
                .filter(r -> productId == null || r.productId().equals(productId))
                .filter(r -> minRating == null || r.rating() >= minRating)
                .sorted(Comparator.comparing(Review::id))
                .toList();
    }

    public Review findById(String id) {
        Review review = reviews.get(id);
        if (review == null) {
            throw new ResourceNotFoundException("Review", id);
        }
        return review;
    }

    public Review create(ReviewCreateRequest request) {
        String id = ids.next();
        Review review = new Review(id, request.productId(), request.authorEmail(), request.rating(),
                request.text(), false, null, OffsetDateTime.now());
        reviews.put(id, review);
        return review;
    }

    public void delete(String id) {
        if (reviews.remove(id) == null) {
            throw new ResourceNotFoundException("Review", id);
        }
    }

    public Review moderate(String id, ModerationRequest request) {
        Review moderated = findById(id).withModeration(request.published(), request.note());
        reviews.put(id, moderated);
        return moderated;
    }

    public int size() {
        return reviews.size();
    }
}
