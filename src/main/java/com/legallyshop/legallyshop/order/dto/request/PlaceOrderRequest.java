package com.legallyshop.legallyshop.order.dto.request;

import com.legallyshop.legallyshop.order.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class PlaceOrderRequest {

    @NotBlank(message = "Tên người nhận không được trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được trống")
    private String receiverPhone;

    @NotBlank(message = "Địa chỉ giao hàng không được trống")
    private String shippingAddress;

    private PaymentMethod paymentMethod = PaymentMethod.COD;

    private String note;

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    private List<OrderItemRequest> items;

    @Getter
    @Setter
    public static class OrderItemRequest {
        @NotNull(message = "Vui lòng chọn SKU")
        private Long skuId;

        @Min(value = 1, message = "Số lượng phải >= 1")
        private int quantity;
    }
}

