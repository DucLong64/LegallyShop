package com.legallyshop.legallyshop.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;

@Getter
@Builder
public class CartResponse {
    private int totalItems;
    private BigDecimal totalPrice;
    private List<CartItemResponse> items;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CartItemResponse {
        private Long skuId;
        private String productName;
        private String skuCode;
        private String skuOptions;
        private BigDecimal price;
        private int quantity;
        private BigDecimal subtotal;
        private String imageUrl;
    }
}