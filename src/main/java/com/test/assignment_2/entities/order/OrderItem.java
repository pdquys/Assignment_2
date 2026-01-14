package com.test.assignment_2.entities.order;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_items_order", columnList = "order_id")
})
public class OrderItem {

  @Id
  @GeneratedValue
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_items_order"))
  private Order order;

  @Column(name = "variant_id", nullable = false)
  private UUID variantId;

  @Column(name = "sku_code", nullable = false, length = 80)
  private String skuCode;

  @Column(name = "product_name", nullable = false, length = 200)
  private String productName;

  @Column(name = "variant_name", nullable = false, length = 100)
  private String variantName; // ex: "Black / L"

  @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "quantity", nullable = false)
  private int quantity;

  @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal lineTotal;
}
