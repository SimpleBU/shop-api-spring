package com.example.shop.web;

import com.example.shop.dto.PageResponse;
import com.example.shop.dto.PriceUpdateRequest;
import com.example.shop.dto.Product;
import com.example.shop.dto.ProductCategory;
import com.example.shop.dto.ProductRequest;
import com.example.shop.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<Product>> list(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Product> found = productService.findAll(category, query);
        return ResponseEntity.ok(PageResponse.of(found, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable String id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        Product created = productService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/products/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> replace(@PathVariable String id,
                                           @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.replace(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}/availability", method = RequestMethod.HEAD)
    public ResponseEntity<Void> availability(@PathVariable String id) {
        boolean available = productService.isAvailable(id);
        return ResponseEntity.status(available ? HttpStatus.NO_CONTENT : HttpStatus.NOT_FOUND)
                .header("X-Stock-Available", Boolean.toString(available))
                .build();
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Product> updatePrice(@PathVariable String id,
                                               @Valid @RequestBody PriceUpdateRequest request) {
        return ResponseEntity.ok(productService.updatePrice(id, request.price()));
    }
}
