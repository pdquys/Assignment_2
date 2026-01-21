package com.test.assignment_2.dto.admin;

import com.test.assignment_2.entities.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {}
