package com.test.assignment_2.entities.catalog;
import com.hunghypebeast.ecommerce.domain.common.AuditableEntity;
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
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(name = "uk_products_slug", columnNames = "slug")
})
public class Product extends AuditableEntity {

  @Id
  @GeneratedValue
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_products_category"))
  private Category category;

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Column(name = "slug", nullable = false, length = 220)
  private String slug;

  @Column(name = "description", columnDefinition = "text")
  private String description;

  @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal basePrice;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Version
  @Column(name = "version", nullable = false)
  private long version;
}
