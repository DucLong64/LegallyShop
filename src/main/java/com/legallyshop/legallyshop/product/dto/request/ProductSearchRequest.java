package com.legallyshop.legallyshop.product.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSearchRequest {

    /**
     * Từ khóa tìm trong tên sản phẩm
     */
    private String keyword;

    /**
     * Lọc theo danh mục
     */
    private Long categoryId;

    /**
     * Khoảng giá
     */
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    /**
     * Filter theo attribute — AND logic: tất cả điều kiện phải thỏa mãn.
     * VD: [{ templateId: 1, value: "8GB" }, { templateId: 3, value: "iOS" }]
     * → chỉ lấy sản phẩm có RAM=8GB VÀ Hệ điều hành=iOS
     */
    private List<AttributeFilter> attributes;

    /**
     * Chỉ lấy sản phẩm còn hàng (có ít nhất 1 SKU stockQty > 0)
     */
    private Boolean inStockOnly;

    @Getter
    @Setter
    public static class AttributeFilter {
        private Long templateId;
        private String value;
    }
}
