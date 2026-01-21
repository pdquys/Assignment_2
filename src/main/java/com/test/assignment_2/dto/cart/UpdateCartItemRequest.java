package com.test.assignment_2.dto.cart;

import jakarta.validation.constraints.Min;

public record UpdateCartItemRequest(
    @Min(1) int quantity
) {}
