package com.legallyshop.legallyshop.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@Getter
@Builder
public class OrderDetailResponse {
    private Long id;
    private String orderCode;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private String receiverName;
    private String receiverPhone;
    private String shippingAddress;
    private String note;
    private List<ItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class ItemResponse {
        private Long id;
        private Long skuId;
        private String productName;
        private String skuOptions;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
