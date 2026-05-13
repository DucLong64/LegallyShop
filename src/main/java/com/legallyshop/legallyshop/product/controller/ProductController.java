package com.legallyshop.legallyshop.product.controller;

import com.legallyshop.legallyshop.common.response.ApiResponse;
import com.legallyshop.legallyshop.product.dto.request.ProductCreateRequest;
import com.legallyshop.legallyshop.product.dto.request.ProductSearchRequest;
import com.legallyshop.legallyshop.product.dto.request.ProductUpdateRequest;
import com.legallyshop.legallyshop.product.dto.response.ProductDetailResponse;
import com.legallyshop.legallyshop.product.dto.response.ProductSummaryResponse;
import com.legallyshop.legallyshop.product.entity.ProductStatus;
import com.legallyshop.legallyshop.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Quản lý sản phẩm")
public class ProductController {

    private final ProductService productService;

    // ════════════════════════════════════════════════════════════════════════
    // Public
    // ════════════════════════════════════════════════════════════════════════

    @GetMapping
    @Operation(summary = "Danh sách sản phẩm — filter đơn giản theo category và keyword")
    public ApiResponse<Page<ProductSummaryResponse>> getProducts(
            @RequestParam(required = false) Long   categoryId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(productService.findAll(categoryId, keyword, pageable));
    }

    @PostMapping("/search")
    @Operation(
            summary = "Search nâng cao — filter theo giá, attribute, tồn kho",
            description = """
            Hỗ trợ filter:
            - keyword: tìm theo tên
            - categoryId: lọc theo danh mục
            - minPrice / maxPrice: khoảng giá
            - inStockOnly: chỉ lấy sản phẩm còn hàng
            - attributes: filter theo thuộc tính (AND logic)
              VD: [{ templateId: 1, value: "8GB" }, { templateId: 3, value: "iOS" }]

            Sort hỗ trợ: createdAt, name
            (sort theo giá cần dùng filter minPrice/maxPrice vì giá nằm ở SKU)
            """)
    public ApiResponse<Page<ProductSummaryResponse>> search(
            @RequestBody(required = false) ProductSearchRequest req,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        if (req == null) req = new ProductSearchRequest();
        return ApiResponse.ok(productService.search(req, pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Chi tiết sản phẩm theo slug")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable String slug) {
        return ApiResponse.ok(productService.findBySlug(slug));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Admin
    // ════════════════════════════════════════════════════════════════════════

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Chi tiết sản phẩm theo ID — bao gồm cả DRAFT/INACTIVE")
    public ApiResponse<ProductDetailResponse> getProductById(@PathVariable Long id) {
        return ApiResponse.ok(productService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Tạo sản phẩm mới — status mặc định là DRAFT")
    public ApiResponse<ProductDetailResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest req) {
        return ApiResponse.created(productService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Cập nhật thông tin sản phẩm (tên, mô tả, danh mục, attributes)")
    public ApiResponse<ProductDetailResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest req) {
        return ApiResponse.ok(productService.update(id, req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "[Admin] Đổi trạng thái sản phẩm",
            description = "DRAFT → ACTIVE (publish) | ACTIVE → INACTIVE (ẩn) | INACTIVE → ACTIVE (hiện lại)")
    public ApiResponse<ProductDetailResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status) {
        return ApiResponse.ok(productService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Xóa mềm sản phẩm — set isActive=false")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok(null);
    }
}
