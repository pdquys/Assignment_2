package com.test.assignment_2.entities.inventory;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.test.assignment_2.entities.common.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory_reservations", uniqueConstraints = {
    @UniqueConstraint(name = "uk_reservations_token", columnNames = "token")
}, indexes = {
    @Index(name = "idx_reservations_status_expires", columnList = "status,expires_at")
})
public class InventoryReservation extends AuditableEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "token", nullable = false)
  private UUID token;

  @Column(name = "cart_token", nullable = false)
  private UUID cartToken;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 16)
  private ReservationStatus status;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<InventoryReservationItem> items = new ArrayList<>();

  public boolean isExpired(Instant now) {
    return expiresAt.isBefore(now) || expiresAt.equals(now);
  }

  public void addItem(InventoryReservationItem item) {
    items.add(item);
    item.setReservation(this);
  }
}
