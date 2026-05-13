package com.legallyshop.legallyshop.product.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustRequest {

    /**
     * Số lượng điều chỉnh:
     *  - Dương (+10): nhập thêm hàng vào kho
     *  - Âm (-5): xuất kho thủ công (hàng hỏng, kiểm kê lại...)
     */
    @NotNull(message = "Số lượng điều chỉnh không được trống")
    private Integer delta;

    /** Lý do điều chỉnh — ghi chú cho admin */
    private String reason;
}
