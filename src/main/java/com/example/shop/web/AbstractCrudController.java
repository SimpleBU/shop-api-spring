package com.example.shop.web;

import com.example.shop.dto.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public abstract class AbstractCrudController<T> {

    protected abstract List<T> loadAll();

    protected abstract T loadOne(String id);

    protected abstract void removeOne(String id);

    @GetMapping
    public ResponseEntity<PageResponse<T>> listAll(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(PageResponse.of(loadAll(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> getById(@PathVariable String id) {
        return ResponseEntity.ok(loadOne(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        removeOne(id);
        return ResponseEntity.noContent().build();
    }
}
