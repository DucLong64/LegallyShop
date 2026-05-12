package com.legallyshop.legallyshop.category.controller;

import com.legallyshop.legallyshop.category.dto.response.AttributeTemplateResponse;
import com.legallyshop.legallyshop.category.dto.response.CategoryTreeResponse;
import com.legallyshop.legallyshop.category.service.CategoryService;
import com.legallyshop.legallyshop.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Quản lý danh mục sản phẩm")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lấy cây danh mục (cha → con)")
    public ApiResponse<List<CategoryTreeResponse>> getTree() {
        return ApiResponse.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/{id}/attributes")
    @Operation(summary = "Lấy danh sách thuộc tính theo danh mục")
    public ApiResponse<List<AttributeTemplateResponse>> getAttributes(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.getAttributeTemplates(id));
    }
}
