package com.legallyshop.legallyshop.category.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attribute_template")
@Data
@NoArgsConstructor
public class AttributeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String inputType;   // text | select | number | boolean

    private Boolean isRequired = false;

    private Integer sortOrder = 0;
}

