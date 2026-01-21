package com.test.assignment_2.controller.admin;


import java.util.UUID;

import com.test.assignment_2.dto.PageResponse;
import com.test.assignment_2.dto.admin.AdminOrderSummaryResponse;
import com.test.assignment_2.dto.admin.UpdateOrderStatusRequest;
import com.test.assignment_2.service.impl.OrderServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

  private final OrderServiceImpl orderServiceImpl;

  @GetMapping
  public PageResponse<AdminOrderSummaryResponse> list(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size) {
    return orderServiceImpl.adminList(page, size);
  }

  @PutMapping("/{orderId}/status")
  public AdminOrderSummaryResponse updateStatus(@PathVariable UUID orderId, @Valid @RequestBody UpdateOrderStatusRequest req) {
    return orderServiceImpl.adminUpdateStatus(orderId, req.status());
  }
}
