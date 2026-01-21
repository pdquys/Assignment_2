package com.test.assignment_2.dto.checkout;

import java.util.UUID;

public record PlaceOrderResponse(
    String orderCode,
    UUID trackingToken
) {}
