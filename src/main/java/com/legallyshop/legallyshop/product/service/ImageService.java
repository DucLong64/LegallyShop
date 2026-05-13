package com.legallyshop.legallyshop.product.service;

import com.legallyshop.legallyshop.common.exception.AppException;
import com.legallyshop.legallyshop.product.dto.request.ImageReorderRequest;
import com.legallyshop.legallyshop.product.dto.response.ProductDetailResponse;
import com.legallyshop.legallyshop.product.entity.Product;
import com.legallyshop.legallyshop.product.entity.ProductImage;
import com.legallyshop.legallyshop.product.repository.ProductImageRepository;
import com.legallyshop.legallyshop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ProductRepository productRepo;
    private final ProductImageRepository imageRepo;
    private final CloudinaryService cloudinaryService;

    // ─── Queries ─────────────────────────────────────────────────────────────

    public List<ProductDetailResponse.ImageResponse> getImages(Long productId) {
        findProductById(productId);
        return imageRepo.findByProductIdOrderBySortOrderAsc(productId)
                .stream().map(this::toResponse).toList();
    }

    // ─── Commands ────────────────────────────────────────────────────────────

    /**
     * Upload ảnh mới cho sản phẩm.
     * Nếu chưa có ảnh nào → ảnh đầu tiên tự động là primary.
     */
    @Transactional
    public ProductDetailResponse.ImageResponse uploadImage(Long productId,
                                                           MultipartFile file,
                                                           Long skuId) {
        Product product = findProductById(productId);

        // Upload lên Cloudinary
        String url = cloudinaryService.upload(file, "products/" + productId);

        // Ảnh đầu tiên của product tự động là primary
        boolean isFirstImage = imageRepo.findByProductIdOrderBySortOrderAsc(productId).isEmpty();
        int sortOrder = imageRepo.findByProductIdOrderBySortOrderAsc(productId).size();

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(url);
        image.setIsPrimary(isFirstImage);
        image.setSortOrder(sortOrder);

        // Gắn với SKU nếu có (ảnh biến thể)
        if (skuId != null) {
            product.getSkus().stream()
                    .filter(s -> s.getId().equals(skuId))
                    .findFirst()
                    .ifPresent(image::setSku);
        }

        return toResponse(imageRepo.save(image));
    }

    /**
     * Đặt ảnh làm ảnh đại diện (primary).
     * Reset tất cả ảnh còn lại về isPrimary=false.
     */
    @Transactional
    public ProductDetailResponse.ImageResponse setPrimary(Long productId, Long imageId) {
        findProductById(productId);
        ProductImage image = findImageByIdAndProduct(imageId, productId);

        imageRepo.clearPrimaryByProductId(productId);
        image.setIsPrimary(true);

        return toResponse(imageRepo.save(image));
    }

    /**
     * Sắp xếp lại thứ tự ảnh (drag & drop).
     * ID đầu tiên trong list tự động trở thành primary.
     */
    @Transactional
    public List<ProductDetailResponse.ImageResponse> reorder(Long productId,
                                                             ImageReorderRequest req) {
        findProductById(productId);
        imageRepo.clearPrimaryByProductId(productId);

        List<Long> ids = req.getImageIds();
        for (int i = 0; i < ids.size(); i++) {
            final int order = i;
            final boolean isPrimary = (i == 0);   // ảnh đầu tiên = primary
            imageRepo.findByIdAndProductId(ids.get(i), productId).ifPresent(img -> {
                img.setSortOrder(order);
                img.setIsPrimary(isPrimary);
            });
        }

        return getImages(productId);
    }

    /**
     * Xóa ảnh — xóa cả trên Cloudinary và DB.
     * Nếu xóa ảnh primary → tự động set ảnh tiếp theo làm primary.
     */
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        findProductById(productId);
        ProductImage image = findImageByIdAndProduct(imageId, productId);

        boolean wasPrimary = Boolean.TRUE.equals(image.getIsPrimary());

        // Xóa khỏi Cloudinary
        cloudinaryService.delete(image.getUrl());

        // Xóa khỏi DB
        imageRepo.delete(image);

        // Nếu là ảnh primary, tự động set ảnh còn lại đầu tiên làm primary
        if (wasPrimary) {
            imageRepo.findByProductIdOrderBySortOrderAsc(productId)
                    .stream().findFirst()
                    .ifPresent(next -> {
                        next.setIsPrimary(true);
                        imageRepo.save(next);
                    });
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Product findProductById(Long productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
    }

    private ProductImage findImageByIdAndProduct(Long imageId, Long productId) {
        return imageRepo.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> AppException.notFound("Ảnh"));
    }

    private ProductDetailResponse.ImageResponse toResponse(ProductImage i) {
        return ProductDetailResponse.ImageResponse.builder()
                .id(i.getId())
                .url(i.getUrl())
                .isPrimary(i.getIsPrimary())
                .sortOrder(i.getSortOrder())
                .build();
    }
}

