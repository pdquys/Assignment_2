package com.test.assignment_2.entities.catalog;


import java.math.BigDecimal;
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
@Table(name = "product_variants", uniqueConstraints = {
    @UniqueConstraint(name = "uk_variants_sku_code", columnNames = "sku_code")
}, indexes = {
    @Index(name = "idx_variants_product", columnList = "product_id")
})
public class ProductVariant extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_variants_product"))
  private Product product;

  @Column(name = "sku_code", nullable = false, length = 80)
  private String skuCode;

  @Column(name = "color", nullable = false, length = 40)
  private String color;

  @Column(name = "size", nullable = false, length = 16)
  private String size;

  @Column(name = "price", nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @Column(name = "stock_on_hand", nullable = false)
  private int stockOnHand;

  @Column(name = "stock_reserved", nullable = false)
  private int stockReserved;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  public int availableStock() {
    return Math.max(0, stockOnHand - stockReserved);
  }
}
