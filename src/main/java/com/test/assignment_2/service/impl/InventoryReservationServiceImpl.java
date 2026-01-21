package com.test.assignment_2.service.impl;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.test.assignment_2.config.AppProperties;
import com.test.assignment_2.dto.checkout.ReserveResponse;
import com.test.assignment_2.entities.inventory.InventoryReservation;
import com.test.assignment_2.entities.inventory.InventoryReservationItem;
import com.test.assignment_2.entities.inventory.ReservationStatus;
import com.test.assignment_2.exception.BadRequestException;
import com.test.assignment_2.exception.NotFoundException;
import com.test.assignment_2.repository.CartRepository;
import com.test.assignment_2.repository.InventoryReservationRepository;
import com.test.assignment_2.repository.ProductVariantRepository;
import com.test.assignment_2.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryReservationServiceImpl implements InventoryReservationService {

  private final AppProperties properties;
  private final CartRepository cartRepository;
  private final ProductVariantRepository variantRepository;
  private final InventoryReservationRepository reservationRepository;

  @Override
  @Transactional
  public ReserveResponse reserveFromCart(UUID cartToken, Integer holdMinutes) {
    int minutes = (holdMinutes != null && holdMinutes >= 10 && holdMinutes <= 30)
        ? holdMinutes
        : properties.getInventory().getDefaultHoldMinutes();

    var now = Instant.now();

    reservationRepository.findFirstByCartTokenAndStatusOrderByCreatedAtDesc(cartToken, ReservationStatus.ACTIVE)
        .filter(r -> !r.isExpired(now))
        .ifPresent(r -> { throw new BadRequestException("RESERVATION_ALREADY_ACTIVE", "There is already an active reservation for this cart"); });

    var cart = cartRepository.findByTokenWithItems(cartToken)
        .orElseThrow(() -> new NotFoundException("Cart not found"));

    if (cart.getItems().isEmpty()) {
      throw new BadRequestException("CART_EMPTY", "Cart is empty");
    }

    var reservation = InventoryReservation.builder()
        .token(UUID.randomUUID())
        .cartToken(cartToken)
        .status(ReservationStatus.ACTIVE)
        .expiresAt(now.plus(minutes, ChronoUnit.MINUTES))
        .build();

    for (var item : cart.getItems()) {
      var variantId = item.getVariant().getId();
      var qty = item.getQuantity();

      var variant = variantRepository.findByIdForUpdate(variantId)
          .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));

      if (!variant.isActive()) {
        throw new BadRequestException("VARIANT_INACTIVE", "Variant is not available: " + variant.getSkuCode());
      }

      if (qty > variant.availableStock()) {
        throw new BadRequestException("INSUFFICIENT_STOCK", "Not enough stock for SKU " + variant.getSkuCode());
      }

      variant.setStockReserved(variant.getStockReserved() + qty);

      reservation.addItem(InventoryReservationItem.builder()
          .variant(variant)
          .quantity(qty)
          .build());
    }

    reservationRepository.save(reservation);
    return new ReserveResponse(reservation.getToken(), reservation.getExpiresAt());
  }

  @Override
  @Transactional
  public void cancelReservation(UUID reservationToken) {
    var now = Instant.now();
    var reservation = reservationRepository.findByTokenWithItems(reservationToken)
        .orElseThrow(() -> new NotFoundException("Reservation not found"));

    if (reservation.getStatus() != ReservationStatus.ACTIVE) return;

    if (reservation.isExpired(now)) {
      releaseReservationInternal(reservation, ReservationStatus.EXPIRED);
      return;
    }
    releaseReservationInternal(reservation, ReservationStatus.CANCELLED);
  }

  @Override
  @Scheduled(fixedDelay = 30_000) // every 30s
  public void releaseExpiredReservations() {
    try {
      var now = Instant.now();
      var expired = reservationRepository.findTop100ByStatusAndExpiresAtBeforeOrderByExpiresAtAsc(ReservationStatus.ACTIVE, now);
      for (var r : expired) {
        releaseById(r.getToken());
      }
    } catch (Exception e) {
      log.warn("releaseExpiredReservations failed: {}", e.getMessage());
    }
  }

  @Override
  @Transactional
  public void releaseById(UUID reservationToken) {
    var now = Instant.now();
    var reservation = reservationRepository.findByTokenWithItems(reservationToken)
        .orElse(null);
    if (reservation == null) return;
    if (reservation.getStatus() != ReservationStatus.ACTIVE) return;

    if (reservation.isExpired(now)) {
      releaseReservationInternal(reservation, ReservationStatus.EXPIRED);
    }
  }

  private void releaseReservationInternal(InventoryReservation reservation, ReservationStatus finalStatus) {
    for (var item : reservation.getItems()) {
      var variantId = item.getVariant().getId();
      var qty = item.getQuantity();

      var variant = variantRepository.findByIdForUpdate(variantId)
          .orElseThrow(() -> new NotFoundException("Variant not found: " + variantId));

      int newReserved = variant.getStockReserved() - qty;
      variant.setStockReserved(Math.max(0, newReserved));
    }
    reservation.setStatus(finalStatus);
    reservationRepository.save(reservation);
  }
}
