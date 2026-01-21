package com.test.assignment_2.dto.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
    UUID variantId,
    String skuCode,
    String productName,
    String variantName,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal lineTotal
) {}
