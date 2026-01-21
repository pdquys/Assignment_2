package com.test.assignment_2.repository;


import java.util.Optional;
import java.util.UUID;

import com.test.assignment_2.entities.catalog.ProductVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select v from ProductVariant v where v.id = :id")
  Optional<ProductVariant> findByIdForUpdate(@Param("id") UUID id);

  Optional<ProductVariant> findBySkuCode(String skuCode);

  java.util.List<ProductVariant> findByProductId(java.util.UUID productId);
}
