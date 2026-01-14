package com.test.assignment_2.entities.cart;

import com.hunghypebeast.ecommerce.domain.common.AuditableEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "carts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_carts_token", columnNames = "token")
})
public class Cart extends AuditableEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "token", nullable = false)
  private UUID token;

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<CartItem> items = new ArrayList<>();

  public void addItem(CartItem item) {
    items.add(item);
    item.setCart(this);
  }
}
