package com.test.assignment_2.repository;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.test.assignment_2.entities.inventory.InventoryReservation;
import com.test.assignment_2.entities.inventory.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, UUID> {

  @Query("select r from InventoryReservation r left join fetch r.items it left join fetch it.variant v left join fetch v.product p where r.token = :token")
  Optional<InventoryReservation> findByTokenWithItems(@Param("token") UUID token);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select r from InventoryReservation r where r.token = :token")
  Optional<InventoryReservation> findByTokenForUpdate(@Param("token") UUID token);

  List<InventoryReservation> findTop100ByStatusAndExpiresAtBeforeOrderByExpiresAtAsc(ReservationStatus status, Instant now);

  java.util.Optional<InventoryReservation> findFirstByCartTokenAndStatusOrderByCreatedAtDesc(java.util.UUID cartToken, ReservationStatus status);
}
