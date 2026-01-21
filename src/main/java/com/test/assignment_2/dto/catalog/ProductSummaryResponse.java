package com.test.assignment_2.dto.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResponse(
    UUID id,
    String name,
    String slug,
    String categorySlug,
    BigDecimal minPrice,
    BigDecimal maxPrice
) {}
