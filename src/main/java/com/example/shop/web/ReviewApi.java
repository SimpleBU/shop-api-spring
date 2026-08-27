package com.example.shop.web;

import com.example.shop.dto.ModerationRequest;
import com.example.shop.dto.PageResponse;
import com.example.shop.dto.Review;
import com.example.shop.dto.ReviewCreateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/reviews")
public interface ReviewApi {

    @GetMapping
    ResponseEntity<PageResponse<Review>> list(@RequestParam(required = false) String productId,
                                              @RequestParam(required = false) Integer minRating,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size);

    @GetMapping("/{id}")
    ResponseEntity<Review> getOne(@PathVariable String id);

    @PostMapping
    ResponseEntity<Review> create(@Valid @RequestBody ReviewCreateRequest request);

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable String id);

    @PatchMapping("/{id}/moderate")
    ResponseEntity<Review> moderate(@PathVariable String id, @Valid @RequestBody ModerationRequest request);
}
