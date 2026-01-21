package com.test.assignment_2.dto.checkout;

import java.util.UUID;

import com.test.assignment_2.entities.order.PaymentMethod;
import jakarta.validation.constraints.*;

public record PlaceOrderRequest(
    @NotNull UUID reservationToken,

    @Email @NotBlank String email,
    @NotBlank String fullName,
    @NotBlank String phone,

    @NotBlank String addressLine1,
    String addressLine2,
    @NotBlank String city,

    @NotNull PaymentMethod paymentMethod
) {}
