package com.legallyshop.legallyshop.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Builder
public class OrderSummaryResponse {
    private Long id;
    private String orderCode;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private int itemCount;
    private LocalDateTime createdAt;
}
