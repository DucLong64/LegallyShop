package com.legallyshop.legallyshop.order.entity;

import com.legallyshop.legallyshop.product.entity.Sku;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    // Snapshot tại thời điểm đặt hàng
    private String     productName;
    private String     skuOptions;    // vd: "Đen titan / 256GB"
    private Integer    quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}