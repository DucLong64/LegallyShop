package com.legallyshop.legallyshop.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddCartItemRequest {

    @NotNull(message = "Vui lòng chọn sản phẩm")
    private Long skuId;

    @Min(value = 1, message = "Số lượng phải >= 1")
    private int qty = 1;
}
