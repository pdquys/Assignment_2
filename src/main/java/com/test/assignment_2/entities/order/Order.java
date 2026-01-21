package com.test.assignment_2.entities.order;

import java.math.BigDecimal;
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
@Table(name = "orders", uniqueConstraints = {
    @UniqueConstraint(name = "uk_orders_order_code", columnNames = "order_code"),
    @UniqueConstraint(name = "uk_orders_tracking_token", columnNames = "tracking_token")
}, indexes = {
    @Index(name = "idx_orders_status_created", columnList = "status,created_at")
})
public class Order extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "order_code", nullable = false, length = 32)
  private String orderCode;

  @Column(name = "tracking_token", nullable = false)
  private UUID trackingToken;

  @Column(name = "reservation_token", nullable = false)
  private UUID reservationToken;

  @Column(name = "email", nullable = false, length = 200)
  private String email;

  @Column(name = "full_name", nullable = false, length = 120)
  private String fullName;

  @Column(name = "phone", nullable = false, length = 30)
  private String phone;

  @Column(name = "address_line1", nullable = false, length = 220)
  private String addressLine1;

  @Column(name = "address_line2", length = 220)
  private String addressLine2;

  @Column(name = "city", nullable = false, length = 120)
  private String city;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false, length = 20)
  private PaymentMethod paymentMethod;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private OrderStatus status;

  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalAmount;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<OrderItem> items = new ArrayList<>();

  public void addItem(OrderItem item) {
    items.add(item);
    item.setOrder(this);
  }
}
