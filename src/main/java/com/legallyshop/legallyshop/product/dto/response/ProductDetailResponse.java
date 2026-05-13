package com.legallyshop.legallyshop.product.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String status;
    private List<SkuResponse> skus;
    private List<AttributeResponse> attributes;
    private List<ImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class SkuResponse {
        private Long id;
        private String skuCode;
        private BigDecimal price;
        private BigDecimal originalPrice;
        private Integer stockQty;
        private Boolean isActive;
        private List<OptionResponse> options;
    }

    @Getter
    @Builder
    public static class OptionResponse {
        private String optionName;
        private String optionValue;
    }

    @Getter
    @Builder
    public static class AttributeResponse {
        private Long templateId;
        private String templateName;
        private String inputType;
        private String value;
    }

    @Getter
    @Builder
    public static class ImageResponse {
        private Long id;
        private String url;
        private Boolean isPrimary;
        private Integer sortOrder;
    }
}