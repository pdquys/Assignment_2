package com.test.assignment_2.dto.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariantResponse(
    UUID variantId,
    String skuCode,
    String color,
    String size,
    BigDecimal price,
    int availableStock
) {}
