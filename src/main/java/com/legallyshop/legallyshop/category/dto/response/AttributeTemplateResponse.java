package com.legallyshop.legallyshop.category.dto.response;

import lombok.*;

@Getter
@Builder
public class AttributeTemplateResponse {
    private Long id;
    private String name;
    private String inputType;
    private Boolean isRequired;
    private Integer sortOrder;
}