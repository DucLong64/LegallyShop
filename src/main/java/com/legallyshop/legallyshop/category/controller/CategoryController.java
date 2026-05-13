package com.legallyshop.legallyshop.category.controller;

import com.legallyshop.legallyshop.category.dto.request.AttributeTemplateRequest;
import com.legallyshop.legallyshop.category.dto.request.CategoryRequest;
import com.legallyshop.legallyshop.category.dto.request.ReorderRequest;
import com.legallyshop.legallyshop.category.dto.response.AttributeTemplateResponse;
import com.legallyshop.legallyshop.category.dto.response.CategoryDetailResponse;
import com.legallyshop.legallyshop.category.dto.response.CategoryDropdownResponse;
import com.legallyshop.legallyshop.category.dto.response.CategoryTreeResponse;
import com.legallyshop.legallyshop.category.service.CategoryService;
import com.legallyshop.legallyshop.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Quản lý danh mục sản phẩm")
public class CategoryController {

    private final CategoryService categoryService;

    // ════════════════════════════════════════════════════════════════════════
    // Public endpoints
    // ════════════════════════════════════════════════════════════════════════

    @GetMapping("/tree")
    @Operation(summary = "Cây danh mục đầy đủ (cha → con đệ quy)",
            description = "Dùng để render menu điều hướng ở frontend")
    public ApiResponse<List<CategoryTreeResponse>> getTree() {
        return ApiResponse.ok(categoryService.getCategoryTree());
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Lấy danh mục theo slug",
            description = "Dùng cho breadcrumb và trang danh mục theo URL")
    public ApiResponse<CategoryDetailResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(categoryService.getBySlug(slug));
    }

    @GetMapping("/{id}/attributes")
    @Operation(summary = "Lấy danh sách thuộc tính (attribute template) theo danh mục",
            description = "Dùng khi render form tạo sản phẩm — mỗi danh mục có bộ thuộc tính riêng")
    public ApiResponse<List<AttributeTemplateResponse>> getAttributes(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.getAttributeTemplates(id));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Admin — Category CRUD
    // ════════════════════════════════════════════════════════════════════════

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Danh sách phẳng tất cả danh mục",
            description = "Dùng cho dropdown chọn danh mục cha khi tạo/sửa danh mục")
    public ApiResponse<List<CategoryDropdownResponse>> getAll() {
        return ApiResponse.ok(categoryService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Chi tiết một danh mục theo ID",
            description = "Dùng cho form edit — trả về cả children và attribute templates")
    public ApiResponse<CategoryDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Tạo danh mục mới",
            description = "Nếu không truyền slug thì tự sinh từ name. parentId=null → danh mục gốc")
    public ApiResponse<CategoryDetailResponse> create(
            @Valid @RequestBody CategoryRequest req) {
        return ApiResponse.created(categoryService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Cập nhật danh mục",
            description = "Có thể đổi tên, slug, danh mục cha và thứ tự. " +
                    "Hệ thống tự ngăn việc đặt danh mục con làm cha.")
    public ApiResponse<CategoryDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest req) {
        return ApiResponse.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Xóa danh mục",
            description = "Chỉ xóa được nếu danh mục không có con và không có sản phẩm active")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Đổi thứ tự danh mục (drag & drop)",
            description = "Truyền danh sách id theo thứ tự mới. " +
                    "Backend tự gán sortOrder theo index của từng id.")
    public ApiResponse<Void> reorder(@Valid @RequestBody ReorderRequest req) {
        categoryService.reorder(req);
        return ApiResponse.ok(null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Admin — Attribute Template CRUD
    // ════════════════════════════════════════════════════════════════════════

    @PostMapping("/{categoryId}/attributes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Thêm thuộc tính vào danh mục",
            description = "VD: thêm thuộc tính 'RAM' (type=select) vào danh mục Điện thoại")
    public ApiResponse<AttributeTemplateResponse> createTemplate(
            @PathVariable Long categoryId,
            @Valid @RequestBody AttributeTemplateRequest req) {
        return ApiResponse.created(categoryService.createTemplate(categoryId, req));
    }

    @PutMapping("/{categoryId}/attributes/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Cập nhật thuộc tính của danh mục")
    public ApiResponse<AttributeTemplateResponse> updateTemplate(
            @PathVariable Long categoryId,
            @PathVariable Long templateId,
            @Valid @RequestBody AttributeTemplateRequest req) {
        return ApiResponse.ok(categoryService.updateTemplate(categoryId, templateId, req));
    }

    @DeleteMapping("/{categoryId}/attributes/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Xóa thuộc tính khỏi danh mục",
            description = "Lưu ý: xóa template không xóa dữ liệu đã nhập ở product_attribute")
    public ApiResponse<Void> deleteTemplate(
            @PathVariable Long categoryId,
            @PathVariable Long templateId) {
        categoryService.deleteTemplate(categoryId, templateId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{categoryId}/attributes/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Đổi thứ tự thuộc tính trong danh mục (drag & drop)")
    public ApiResponse<Void> reorderTemplates(
            @PathVariable Long categoryId,
            @Valid @RequestBody ReorderRequest req) {
        categoryService.reorderTemplates(categoryId, req);
        return ApiResponse.ok(null);
    }
}