package com.test.assignment_2.repository;


import java.util.Optional;
import java.util.UUID;

import com.test.assignment_2.entities.catalog.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  Optional<Category> findBySlug(String slug);
}
