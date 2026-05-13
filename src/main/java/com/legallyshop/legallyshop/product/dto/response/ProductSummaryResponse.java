package com.legallyshop.legallyshop.product.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Builder
public class ProductSummaryResponse {
    private Long id;
    private String name;
    private String slug;
    private Long categoryId;
    private String categoryName;
    private BigDecimal minPrice;
    private String imageUrl;
    private String status;
    private LocalDateTime createdAt;
}