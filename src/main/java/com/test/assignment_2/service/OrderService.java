package com.test.assignment_2.service;

import com.test.assignment_2.dto.PageResponse;
import com.test.assignment_2.dto.admin.AdminOrderSummaryResponse;
import com.test.assignment_2.dto.order.TrackingOrderResponse;
import com.test.assignment_2.entities.order.Order;
import com.test.assignment_2.entities.order.OrderStatus;
import com.test.assignment_2.entities.order.PaymentMethod;

import java.util.UUID;

public interface OrderService {
    Order placeOrder(
            UUID reservationToken,
            String email,
            String fullName,
            String phone,
            String addressLine1,
            String addressLine2,
            String city,
            PaymentMethod paymentMethod
    );
    TrackingOrderResponse track(UUID trackingToken);
    PageResponse<AdminOrderSummaryResponse> adminList(int page, int size);
    AdminOrderSummaryResponse adminUpdateStatus(UUID orderId, OrderStatus target);
    Order markPaidByOrderCode(String orderCode);

}
