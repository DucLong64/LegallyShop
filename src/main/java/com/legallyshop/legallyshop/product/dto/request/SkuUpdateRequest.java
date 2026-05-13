package com.legallyshop.legallyshop.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkuUpdateRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải > 0")
    private BigDecimal price;

    private BigDecimal originalPrice;  // null = xóa giá gốc (không còn giảm giá)

    private Boolean isActive;
}