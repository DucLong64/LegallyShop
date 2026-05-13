package com.legallyshop.legallyshop.product.controller;


import com.legallyshop.legallyshop.common.response.ApiResponse;
import com.legallyshop.legallyshop.product.dto.request.ImageReorderRequest;
import com.legallyshop.legallyshop.product.dto.response.ProductDetailResponse;
import com.legallyshop.legallyshop.product.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Product Images", description = "Quản lý ảnh sản phẩm")
public class ImageController {

    private final ImageService imageService;

    @GetMapping
    @Operation(summary = "[Admin] Danh sách ảnh của sản phẩm theo thứ tự")
    public ApiResponse<List<ProductDetailResponse.ImageResponse>> getImages(
            @PathVariable Long productId) {
        return ApiResponse.ok(imageService.getImages(productId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "[Admin] Upload ảnh mới cho sản phẩm",
            description = """
            - Ảnh được upload lên Cloudinary, tự động resize về tối đa 1200px.
            - Ảnh đầu tiên của sản phẩm tự động được set làm primary.
            - Truyền skuId nếu ảnh đặc trưng cho 1 biến thể cụ thể (VD: ảnh màu đen).
            - Giới hạn: 5MB, định dạng jpg/png/webp.
            """)
    public ApiResponse<ProductDetailResponse.ImageResponse> uploadImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long skuId) {
        return ApiResponse.created(imageService.uploadImage(productId, file, skuId));
    }

    @PatchMapping("/{imageId}/primary")
    @Operation(
            summary = "[Admin] Đặt ảnh làm ảnh đại diện (primary)",
            description = "Reset tất cả ảnh còn lại về isPrimary=false trước khi set ảnh mới")
    public ApiResponse<ProductDetailResponse.ImageResponse> setPrimary(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        return ApiResponse.ok(imageService.setPrimary(productId, imageId));
    }

    @PatchMapping("/reorder")
    @Operation(
            summary = "[Admin] Sắp xếp lại thứ tự ảnh (drag & drop)",
            description = "ID đầu tiên trong list tự động trở thành primary")
    public ApiResponse<List<ProductDetailResponse.ImageResponse>> reorder(
            @PathVariable Long productId,
            @Valid @RequestBody ImageReorderRequest req) {
        return ApiResponse.ok(imageService.reorder(productId, req));
    }

    @DeleteMapping("/{imageId}")
    @Operation(
            summary = "[Admin] Xóa ảnh",
            description = "Xóa cả trên Cloudinary. Nếu là ảnh primary, ảnh kế tiếp tự động lên làm primary")
    public ApiResponse<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        imageService.deleteImage(productId, imageId);
        return ApiResponse.ok(null);
    }
}

