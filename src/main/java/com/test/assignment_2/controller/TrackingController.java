package com.test.assignment_2.controller;

import java.util.UUID;

import com.test.assignment_2.dto.order.TrackingOrderResponse;
import com.test.assignment_2.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/orders")
public class TrackingController {

  private final OrderServiceImpl orderServiceImpl;

  @GetMapping("/track/{trackingToken}")
  public TrackingOrderResponse track(@PathVariable UUID trackingToken) {
    return orderServiceImpl.track(trackingToken);
  }
}
