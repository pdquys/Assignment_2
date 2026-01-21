package com.test.assignment_2.service.impl;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.test.assignment_2.config.AppProperties;
import com.test.assignment_2.dto.PageResponse;
import com.test.assignment_2.dto.admin.AdminOrderSummaryResponse;
import com.test.assignment_2.dto.order.OrderItemDto;
import com.test.assignment_2.dto.order.TrackingOrderResponse;
import com.test.assignment_2.entities.inventory.ReservationStatus;
import com.test.assignment_2.entities.order.Order;
import com.test.assignment_2.entities.order.OrderItem;
import com.test.assignment_2.entities.order.OrderStatus;
import com.test.assignment_2.entities.order.PaymentMethod;
import com.test.assignment_2.exception.BadRequestException;
import com.test.assignment_2.exception.NotFoundException;
import com.test.assignment_2.repository.InventoryReservationRepository;
import com.test.assignment_2.repository.OrderRepository;
import com.test.assignment_2.repository.ProductVariantRepository;
import com.test.assignment_2.service.OrderService;
import com.test.assignment_2.util.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final AppProperties properties;
  private final InventoryReservationRepository reservationRepository;
  private final ProductVariantRepository variantRepository;
  private final OrderRepository orderRepository;
  private final MailService mailService;

  @Override
  @Transactional
  public Order placeOrder(
      UUID reservationToken,
      String email,
      String fullName,
      String phone,
      String addressLine1,
      String addressLine2,
      String city,
      PaymentMethod paymentMethod
  ) {
    var now = Instant.now();

    var reservation = reservationRepository.findByTokenWithItems(reservationToken)
        .orElseThrow(() -> new NotFoundException("Reservation not found"));

    if (reservation.getStatus() != ReservationStatus.ACTIVE) {
      throw new BadRequestException("RESERVATION_NOT_ACTIVE", "Reservation is not active");
    }
    if (reservation.isExpired(now)) {
      throw new BadRequestException("RESERVATION_EXPIRED", "Reservation expired");
    }

    // lock reservation row to avoid double-consume
    reservationRepository.findByTokenForUpdate(reservationToken)
        .orElseThrow(() -> new NotFoundException("Reservation not found"));

    // idempotency: if order already created for this reservation token, return existing
    var existing = orderRepository.findByReservationToken(reservationToken);
    if (existing.isPresent()) return existing.get();

    var order = Order.builder()
        .orderCode(generateOrderCode(now))
        .trackingToken(UUID.randomUUID())
        .reservationToken(reservationToken)
        .email(email)
        .fullName(fullName)
        .phone(phone)
        .addressLine1(addressLine1)
        .addressLine2(addressLine2)
        .city(city)
        .paymentMethod(paymentMethod)
        .status(paymentMethod == PaymentMethod.COD ? OrderStatus.CONFIRMED : OrderStatus.PENDING_PAYMENT)
        .totalAmount(BigDecimal.ZERO)
        .build();

    BigDecimal total = BigDecimal.ZERO;

    for (var item : reservation.getItems()) {
      var variantId = item.getVariant().getId();
      var qty = item.getQuantity();

      var variant = variantRepository.findByIdForUpdate(variantId)
          .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));

      if (qty > variant.getStockReserved()) {
        throw new BadRequestException("RESERVED_MISMATCH", "Reserved stock mismatch for SKU " + variant.getSkuCode());
      }
      if (qty > variant.getStockOnHand()) {
        throw new BadRequestException("STOCK_CHANGED", "Stock changed unexpectedly for SKU " + variant.getSkuCode());
      }

      variant.setStockReserved(variant.getStockReserved() - qty);
      variant.setStockOnHand(variant.getStockOnHand() - qty);

      var unit = variant.getPrice();
      var line = unit.multiply(BigDecimal.valueOf(qty));
      total = total.add(line);

      order.addItem(OrderItem.builder()
          .variantId(variant.getId())
          .skuCode(variant.getSkuCode())
          .productName(variant.getProduct().getName())
          .variantName(variant.getColor() + " / " + variant.getSize())
          .unitPrice(unit)
          .quantity(qty)
          .lineTotal(line)
          .build());
    }

    order.setTotalAmount(total);

    reservation.setStatus(ReservationStatus.CONSUMED);
    reservationRepository.save(reservation);

    orderRepository.save(order);

    // Send email after successful commit
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          sendConfirmationEmail(order);
        }
      });
    } else {
      sendConfirmationEmail(order);
    }

    return order;
  }

  @Override
  @Transactional(readOnly = true)
  public TrackingOrderResponse track(UUID trackingToken) {
    var order = orderRepository.findByTrackingToken(trackingToken)
        .orElseThrow(() -> new NotFoundException("Order not found"));

    var items = order.getItems().stream()
        .map(i -> new OrderItemDto(i.getVariantId(), i.getSkuCode(), i.getProductName(), i.getVariantName(), i.getUnitPrice(), i.getQuantity(), i.getLineTotal()))
        .toList();

    return new TrackingOrderResponse(order.getOrderCode(), order.getStatus(), order.getPaymentMethod(), order.getTotalAmount(), order.getCreatedAt(), items);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<AdminOrderSummaryResponse> adminList(int page, int size) {
    var pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
    var p = orderRepository.findAll(pageable).map(o -> new AdminOrderSummaryResponse(
        o.getId(), o.getOrderCode(), o.getStatus(), o.getPaymentMethod(), o.getTotalAmount(), o.getCreatedAt(), o.getFullName(), o.getPhone()
    ));
    return PageResponse.from(p);
  }

  @Override
  @Transactional
  public AdminOrderSummaryResponse adminUpdateStatus(UUID orderId, OrderStatus target) {
    var order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
    var current = order.getStatus();

    if (!isValidTransition(current, target)) {
      throw new BadRequestException("INVALID_STATUS_TRANSITION", "Cannot change status from " + current + " to " + target);
    }

    if (target == OrderStatus.CANCELLED && current != OrderStatus.CANCELLED) {
      // Restock items (Phase 1 simplification)
      for (var item : order.getItems()) {
        var variant = variantRepository.findByIdForUpdate(item.getVariantId())
            .orElseThrow(() -> new NotFoundException("Variant not found: " + item.getVariantId()));
        variant.setStockOnHand(variant.getStockOnHand() + item.getQuantity());
      }
    }

    order.setStatus(target);
    var saved = orderRepository.save(order);
    return new AdminOrderSummaryResponse(saved.getId(), saved.getOrderCode(), saved.getStatus(), saved.getPaymentMethod(), saved.getTotalAmount(), saved.getCreatedAt(), saved.getFullName(), saved.getPhone());
  }

  @Override
  @Transactional
  public Order markPaidByOrderCode(String orderCode) {
    var order = orderRepository.findByOrderCode(orderCode)
        .orElseThrow(() -> new NotFoundException("Order not found"));
    if (order.getStatus() == OrderStatus.CANCELLED) {
      throw new BadRequestException("ORDER_CANCELLED", "Order is cancelled");
    }
    order.setStatus(OrderStatus.PAID);
    return orderRepository.save(order);
  }

  private boolean isValidTransition(OrderStatus from, OrderStatus to) {
    if (from == to) return true;
    return switch (from) {
      case PENDING_PAYMENT -> (to == OrderStatus.PAID) || (to == OrderStatus.CANCELLED) || (to == OrderStatus.CONFIRMED);
      case CONFIRMED -> (to == OrderStatus.SHIPPING) || (to == OrderStatus.CANCELLED) || (to == OrderStatus.PAID);
      case PAID -> (to == OrderStatus.SHIPPING) || (to == OrderStatus.CANCELLED);
      case SHIPPING -> false;
      case CANCELLED -> false;
    };
  }

  private void sendConfirmationEmail(Order order) {
    String link = properties.getTracking().getBaseUrl() + "/" + order.getTrackingToken();
    String subject = "Xác nhận đơn hàng " + order.getOrderCode();
    String body = "Cảm ơn bạn đã mua hàng tại Hung Hypebeast!\n\n"
        + "Mã đơn: " + order.getOrderCode() + "\n"
        + "Trạng thái: " + order.getStatus() + "\n"
        + "Tổng tiền: " + order.getTotalAmount() + "\n\n"
        + "Theo dõi đơn hàng tại: " + link + "\n";
    mailService.sendOrderConfirmation(order.getEmail(), subject, body);
  }

  private String generateOrderCode(Instant now) {
    String date = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(now);
    String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    return "HHB-" + date + "-" + rand;
  }
}
