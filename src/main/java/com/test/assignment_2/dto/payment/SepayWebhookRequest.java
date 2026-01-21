package com.test.assignment_2.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record SepayWebhookRequest(
    @NotBlank String orderCode
) {}
