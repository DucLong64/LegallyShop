package com.legallyshop.legallyshop.category.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CategoryDetailResponse {
    private Long id;
    private String name;
    private String slug;
    private Integer sortOrder;

    // Thông tin cha
    private Long parentId;
    private String parentName;

    // Danh mục con trực tiếp (không đệ quy sâu)
    private List<CategorySimpleResponse> children;

    // Danh sách attribute template của danh mục này
    private List<AttributeTemplateResponse> attributeTemplates;

    private LocalDateTime createdAt;

    // DTO phẳng cho danh mục con — tránh load đệ quy vô hạn
    @Getter
    @Builder
    public static class CategorySimpleResponse {
        private Long id;
        private String name;
        private String slug;
        private Integer sortOrder;
    }
}
