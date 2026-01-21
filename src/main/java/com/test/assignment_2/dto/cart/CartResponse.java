package com.test.assignment_2.dto.cart;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
    UUID cartToken,
    List<CartItemResponse> items,
    BigDecimal totalAmount
) {}
