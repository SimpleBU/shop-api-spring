package com.example.shop.web;

import com.example.shop.dto.ModerationRequest;
import com.example.shop.dto.PageResponse;
import com.example.shop.dto.Review;
import com.example.shop.dto.ReviewCreateRequest;
import com.example.shop.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
public class ReviewController implements ReviewApi {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Override
    public ResponseEntity<PageResponse<Review>> list(String productId, Integer minRating, int page, int size) {
        List<Review> found = reviewService.findAll(productId, minRating);
        return ResponseEntity.ok(PageResponse.of(found, page, size));
    }

    @Override
    public ResponseEntity<Review> getOne(String id) {
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @Override
    public ResponseEntity<Review> create(ReviewCreateRequest request) {
        Review review = reviewService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/reviews/" + review.id())).body(review);
    }

    @Override
    public ResponseEntity<Void> delete(String id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Review> moderate(String id, ModerationRequest request) {
        return ResponseEntity.ok(reviewService.moderate(id, request));
    }
}
