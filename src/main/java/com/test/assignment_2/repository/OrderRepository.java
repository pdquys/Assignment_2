package com.test.assignment_2.repository;


import java.util.Optional;
import java.util.UUID;

import com.test.assignment_2.entities.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {
  Optional<Order> findByTrackingToken(UUID trackingToken);
  Optional<Order> findByOrderCode(String orderCode);
  Optional<Order> findByReservationToken(java.util.UUID reservationToken);
}
