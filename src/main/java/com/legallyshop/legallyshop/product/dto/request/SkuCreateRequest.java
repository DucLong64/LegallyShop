package com.legallyshop.legallyshop.product.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class SkuCreateRequest {

    @NotBlank(message = "Mã SKU không được trống")
    private String skuCode;

    @NotNull(message = "Giá bán không được trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải > 0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @Min(value = 0, message = "Tồn kho phải >= 0")
    private Integer stockQty = 0;

    private List<ProductCreateRequest.SkuOptionDto> options = new ArrayList<>();
}
