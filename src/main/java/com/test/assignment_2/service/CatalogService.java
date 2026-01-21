package com.test.assignment_2.service;

import com.test.assignment_2.dto.PageResponse;
import com.test.assignment_2.dto.catalog.ProductDetailResponse;
import com.test.assignment_2.dto.catalog.ProductSummaryResponse;

import java.math.BigDecimal;

public interface CatalogService {
    PageResponse<ProductSummaryResponse> listProducts(
            int page, int size,
            String categorySlug,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword
    );
    public ProductDetailResponse getProductDetail(String slug);

}
