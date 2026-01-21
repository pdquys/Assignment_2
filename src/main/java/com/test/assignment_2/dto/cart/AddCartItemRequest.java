package com.test.assignment_2.dto.cart;

import java.util.UUID;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
    @NotNull UUID variantId,
    @Min(1) int quantity
) {}
