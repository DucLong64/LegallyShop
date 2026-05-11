package com.legallyshop.legallyshop.product.entity;

import com.legallyshop.legallyshop.category.entity.AttributeTemplate;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_attribute")
@Data
@NoArgsConstructor
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private AttributeTemplate template;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String value;
}
