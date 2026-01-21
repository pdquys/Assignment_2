package com.test.assignment_2.repository;

import java.util.Optional;
import java.util.UUID;

import com.test.assignment_2.entities.cart.Cart;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, UUID> {

  @Query("select c from Cart c left join fetch c.items i left join fetch i.variant v left join fetch v.product p where c.token = :token")
  Optional<Cart> findByTokenWithItems(@Param("token") UUID token);

  Optional<Cart> findByToken(UUID token);
}
