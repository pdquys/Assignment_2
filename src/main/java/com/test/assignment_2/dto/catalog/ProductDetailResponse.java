package com.test.assignment_2.dto.catalog;

import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(
    UUID id,
    String name,
    String slug,
    String description,
    String categorySlug,
    List<ProductVariantResponse> variants
) {}
