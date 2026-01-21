package com.test.assignment_2.controller;


import java.math.BigDecimal;

import com.test.assignment_2.dto.PageResponse;
import com.test.assignment_2.dto.catalog.ProductDetailResponse;
import com.test.assignment_2.dto.catalog.ProductSummaryResponse;
import com.test.assignment_2.service.impl.CatalogServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/products")
public class CatalogController {

  private final CatalogServiceImpl catalogService;

  @GetMapping
  public PageResponse<ProductSummaryResponse> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) String keyword
  ) {
    return catalogService.listProducts(page, size, category, minPrice, maxPrice, keyword);
  }

  @GetMapping("/{slug}")
  public ProductDetailResponse detail(@PathVariable String slug) {
    return catalogService.getProductDetail(slug);
  }
}
