package com.legallyshop.legallyshop.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sku_option")
@Data
@NoArgsConstructor
public class SkuOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private Sku sku;

    @Column(nullable = false)
    private String optionName;   // "Màu sắc", "Dung lượng"

    @Column(nullable = false)
    private String optionValue;  // "Đen titan", "256GB"
}