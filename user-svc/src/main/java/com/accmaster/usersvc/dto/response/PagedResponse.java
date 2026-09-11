package com.accmaster.usersvc.dto.response;
import java.util.List;
/** Standardized paged response envelope. Replaces PHP PagedResponse class. */
public record PagedResponse<T>(
    List<T> items,
    int page,
    int size,
    long totalItems,
    int totalPages,
    String sortBy,
    String sortDir
) {}
