package com.legallyshop.legallyshop.category.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDropdownResponse {
    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private Integer sortOrder;
    private Integer level; // 0 = root, 1 = level 1, 2 = level 2...
}
