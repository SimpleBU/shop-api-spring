package com.example.shop.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static <T> PageResponse<T> of(List<T> all, int page, int size) {
        int safeSize = size <= 0 ? 20 : size;
        int safePage = Math.max(page, 0);
        int from = Math.min(safePage * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        int totalPages = (int) Math.ceil((double) all.size() / safeSize);
        return new PageResponse<>(all.subList(from, to), safePage, safeSize, all.size(), totalPages);
    }
}
