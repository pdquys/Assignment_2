package com.test.assignment_2.exception;
import java.time.Instant;
import java.util.Map;

public record ApiError(
    Instant timestamp,
    String code,
    String message,
    Map<String, Object> details
) {}
