package com.legallyshop.legallyshop.product.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sku")
@Data
@NoArgsConstructor
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(unique = true, nullable = false)
    private String skuCode;       // VD: "IPHONE15-BLACK-256"

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal originalPrice;  // Giá gốc để hiển thị % giảm

    private Integer stockQty = 0;

    private Boolean isActive = true;

    @OneToMany(mappedBy = "sku",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<SkuOption> options = new ArrayList<>();
}