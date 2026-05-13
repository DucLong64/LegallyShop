package com.legallyshop.legallyshop.product.controller;

import com.legallyshop.legallyshop.common.response.ApiResponse;
import com.legallyshop.legallyshop.product.dto.request.SkuCreateRequest;
import com.legallyshop.legallyshop.product.dto.request.SkuUpdateRequest;
import com.legallyshop.legallyshop.product.dto.request.StockAdjustRequest;
import com.legallyshop.legallyshop.product.dto.response.ProductDetailResponse;
import com.legallyshop.legallyshop.product.service.SkuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/skus")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "SKUs", description = "Quản lý biến thể sản phẩm (SKU)")
public class SkuController {

    private final SkuService skuService;

    @GetMapping
    @Operation(summary = "[Admin] Danh sách tất cả SKU của sản phẩm (bao gồm inactive)")
    public ApiResponse<List<ProductDetailResponse.SkuResponse>> getSkus(
            @PathVariable Long productId) {
        return ApiResponse.ok(skuService.getSkusByProduct(productId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "[Admin] Thêm SKU mới vào sản phẩm")
    public ApiResponse<ProductDetailResponse.SkuResponse> addSku(
            @PathVariable Long productId,
            @Valid @RequestBody SkuCreateRequest req) {
        return ApiResponse.created(skuService.addSku(productId, req));
    }

    @PutMapping("/{skuId}")
    @Operation(
            summary = "[Admin] Cập nhật SKU — giá, originalPrice, trạng thái active",
            description = "Không thể đổi skuCode và options sau khi tạo (vì đã có thể có trong đơn hàng cũ). " +
                    "Muốn đổi options → xóa SKU cũ, tạo SKU mới.")
    public ApiResponse<ProductDetailResponse.SkuResponse> updateSku(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @Valid @RequestBody SkuUpdateRequest req) {
        return ApiResponse.ok(skuService.updateSku(productId, skuId, req));
    }

    @DeleteMapping("/{skuId}")
    @Operation(
            summary = "[Admin] Xóa SKU",
            description = "Không thể xóa SKU cuối cùng của sản phẩm")
    public ApiResponse<Void> deleteSku(
            @PathVariable Long productId,
            @PathVariable Long skuId) {
        skuService.deleteSku(productId, skuId);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{skuId}/stock")
    @Operation(
            summary = "[Admin] Điều chỉnh tồn kho thủ công",
            description = "delta > 0: nhập hàng | delta < 0: xuất kho / hàng hỏng / kiểm kê")
    public ApiResponse<ProductDetailResponse.SkuResponse> adjustStock(
            @PathVariable Long productId,
            @PathVariable Long skuId,
            @Valid @RequestBody StockAdjustRequest req) {
        return ApiResponse.ok(skuService.adjustStock(productId, skuId, req));
    }
}

