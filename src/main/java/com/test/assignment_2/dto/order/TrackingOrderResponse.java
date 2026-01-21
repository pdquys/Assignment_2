package com.test.assignment_2.dto.order;


import com.test.assignment_2.entities.order.OrderStatus;
import com.test.assignment_2.entities.order.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TrackingOrderResponse(
    String orderCode,
    OrderStatus status,
    PaymentMethod paymentMethod,
    BigDecimal totalAmount,
    Instant createdAt,
    List<OrderItemDto> items
) {}
