package com.financeapp.user.dto.response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Standardized paged response envelope.
 */
public record PagedResponse<T>(
    List<T> items,
    int page,
    int size,
    long totalItems,
    int totalPages,
    String sortBy,
    String sortDir
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        String sortBy = "";
        String sortDir = "";
        if (page.getSort().isSorted()) {
            Sort.Order order = page.getSort().iterator().next();
            sortBy = order.getProperty();
            sortDir = order.getDirection().name();
        }
        return new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            sortBy,
            sortDir
        );
    }
}
