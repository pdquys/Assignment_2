package com.test.assignment_2.service.impl;



import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.test.assignment_2.dto.PageResponse;
import com.test.assignment_2.dto.catalog.ProductDetailResponse;
import com.test.assignment_2.dto.catalog.ProductSummaryResponse;
import com.test.assignment_2.dto.catalog.ProductVariantResponse;
import com.test.assignment_2.entities.catalog.Product;
import com.test.assignment_2.entities.catalog.ProductVariant;
import com.test.assignment_2.repository.CategoryRepository;
import com.test.assignment_2.repository.ProductRepository;
import com.test.assignment_2.repository.ProductVariantRepository;
import com.test.assignment_2.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

  private final ProductRepository productRepository;
  private final ProductVariantRepository variantRepository;
  private final CategoryRepository categoryRepository;

  @Override
  @Transactional(readOnly = true)
  public PageResponse<ProductSummaryResponse> listProducts(
      int page, int size,
      String categorySlug,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String keyword
  ) {
    var pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
    Specification<Product> spec = (root, query, cb) -> cb.isTrue(root.get("active"));

    if (categorySlug != null && !categorySlug.isBlank()) {
      var catOpt = categoryRepository.findBySlug(categorySlug);
      if (catOpt.isPresent()) {
        UUID catId = catOpt.get().getId();
        spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), catId));
      } else {
        // No such category => empty result by impossible predicate
        spec = spec.and((root, query, cb) -> cb.equal(cb.literal(1), 0));
      }
    }

    if (keyword != null && !keyword.isBlank()) {
      String like = "%" + keyword.trim().toLowerCase() + "%";
      spec = spec.and((root, query, cb) -> cb.or(
          cb.like(cb.lower(root.get("name")), like),
          cb.like(cb.lower(root.get("slug")), like)
      ));
    }

    // Note: price filter uses variants; keep it simple by filtering in memory after fetching page.
    // In production, use a join + group-by/min/max in DB or a materialized view.
    Page<Product> productPage = productRepository.findAll(spec, pageable);

    List<ProductSummaryResponse> mapped = productPage.getContent().stream()
        .map(p -> {
          var vars = variantRepository.findByProductId(p.getId()).stream().filter(ProductVariant::isActive).toList();
          BigDecimal min = vars.stream().map(ProductVariant::getPrice).min(Comparator.naturalOrder()).orElse(p.getBasePrice());
          BigDecimal max = vars.stream().map(ProductVariant::getPrice).max(Comparator.naturalOrder()).orElse(p.getBasePrice());
          return new ProductSummaryResponse(p.getId(), p.getName(), p.getSlug(),
              p.getCategory() != null ? p.getCategory().getSlug() : null, min, max);
        })
        .filter(ps -> {
          if (minPrice != null && ps.minPrice().compareTo(minPrice) < 0 && ps.maxPrice().compareTo(minPrice) < 0) return false;
          if (maxPrice != null && ps.minPrice().compareTo(maxPrice) > 0 && ps.maxPrice().compareTo(maxPrice) > 0) return false;
          return true;
        })
        .toList();

    // price filtering may reduce items; for Phase 1 we keep pagination semantics from DB page (acceptable for training).
    var mappedPage = new PageImpl<>(mapped, pageable, productPage.getTotalElements());
    return PageResponse.from(mappedPage);
  }

  @Override
  @Transactional(readOnly = true)
  public ProductDetailResponse getProductDetail(String slug) {
    var product = productRepository.findAll((root, query, cb) -> cb.and(
        cb.equal(root.get("slug"), slug),
        cb.isTrue(root.get("active"))
    )).stream().findFirst().orElseThrow(() -> new com.test.assignment_2.exception.NotFoundException("Product not found"));

    var variants = variantRepository.findByProductId(product.getId()).stream()
        .filter(ProductVariant::isActive)
        .map(v -> new ProductVariantResponse(v.getId(), v.getSkuCode(), v.getColor(), v.getSize(), v.getPrice(), v.availableStock()))
        .toList();

    return new ProductDetailResponse(product.getId(), product.getName(), product.getSlug(),
        product.getDescription(),
        product.getCategory() != null ? product.getCategory().getSlug() : null,
        variants);
  }
}
