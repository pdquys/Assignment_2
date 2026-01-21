package com.test.assignment_2.exception;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
    var body = new ApiError(Instant.now(), ex.getCode(), ex.getMessage(), Map.of());
    HttpStatus status = "NOT_FOUND".equals(ex.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, Object> details = new HashMap<>();
    var errors = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
        .toList();
    details.put("errors", errors);

    var body = new ApiError(Instant.now(), "VALIDATION_ERROR", "Request validation failed", details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnknown(Exception ex) {
    var body = new ApiError(Instant.now(), "INTERNAL_ERROR", "Unexpected error", Map.of());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
