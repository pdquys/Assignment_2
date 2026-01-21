package com.test.assignment_2.entities.cart;

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
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(name = "uk_cart_items_cart_variant", columnNames = {"cart_id", "variant_id"})
})
public class CartItem {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_cart"))
  private Cart cart;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "variant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_variant"))
  private ProductVariant variant;

  @Column(name = "quantity", nullable = false)
  private int quantity;
}
