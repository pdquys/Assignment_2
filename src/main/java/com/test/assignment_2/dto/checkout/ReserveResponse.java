package com.test.assignment_2.dto.checkout;

import java.time.Instant;
import java.util.UUID;

public record ReserveResponse(
    UUID reservationToken,
    Instant expiresAt
) {}
