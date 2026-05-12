package com.legallyshop.legallyshop.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Tên danh mục không được trống")
    @Size(max = 255, message = "Tên tối đa 255 ký tự")
    private String name;

    // Nếu null → slug tự sinh từ name
    private String slug;

    // Null → danh mục gốc (level 0)
    private Long parentId;

    private Integer sortOrder = 0;
}