package com.legallyshop.legallyshop.category.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
public class CategoryTreeResponse {
    private Long   id;
    private String name;
    private String slug;
    private List<CategoryTreeResponse> children;
}