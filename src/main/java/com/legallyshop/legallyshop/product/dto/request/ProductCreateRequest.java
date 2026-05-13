package com.legallyshop.legallyshop.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductCreateRequest {

    @NotBlank(message = "Tên sản phẩm không được trống")
    private String name;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Long categoryId;

    private String description;

    private List<AttributeValueDto> attributes = new ArrayList<>();

    @NotEmpty(message = "Sản phẩm cần ít nhất 1 SKU")
    private List<SkuDto> skus;

    private List<ImageDto> images = new ArrayList<>();

    @Getter
    @Setter
    public static class AttributeValueDto {
        private Long templateId;
        private String value;
    }

    @Getter
    @Setter
    public static class SkuDto {
        @NotBlank
        private String skuCode;
        @NotNull
        private BigDecimal price;
        private BigDecimal originalPrice;
        @Min(0)
        private Integer stockQty = 0;
        private List<SkuOptionDto> options = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class SkuOptionDto {
        private String optionName;
        private String optionValue;
    }

    @Getter
    @Setter
    public static class ImageDto {
        private String url;
        private Boolean isPrimary = false;
        private Integer sortOrder = 0;
    }
}
