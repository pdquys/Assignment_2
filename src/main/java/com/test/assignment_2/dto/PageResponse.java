package com.test.assignment_2.dto;

import java.util.List;

public record PageResponse<T>(
    List<T> items,
    int page,
    int size,
    long totalItems,
    int totalPages
) {
  public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> p) {
    return new PageResponse<>(p.getContent(), p.getNumber() + 1, p.getSize(), p.getTotalElements(), p.getTotalPages());
  }
}
