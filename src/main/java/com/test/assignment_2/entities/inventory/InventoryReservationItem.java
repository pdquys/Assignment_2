package com.test.assignment_2.entities.inventory;


import java.util.UUID;

import com.test.assignment_2.entities.catalog.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory_reservation_items", indexes = {
    @Index(name = "idx_res_items_reservation", columnList = "reservation_id")
})
public class InventoryReservationItem {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reservation_id", nullable = false, foreignKey = @ForeignKey(name = "fk_res_items_reservation"))
  private InventoryReservation reservation;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_res_items_variant"))
  private ProductVariant variant;

  @Column(name = "quantity", nullable = false)
  private int quantity;
}
