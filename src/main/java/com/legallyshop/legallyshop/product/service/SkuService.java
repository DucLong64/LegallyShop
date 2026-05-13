package com.legallyshop.legallyshop.product.service;

import com.legallyshop.legallyshop.common.exception.AppException;
import com.legallyshop.legallyshop.product.dto.request.SkuCreateRequest;
import com.legallyshop.legallyshop.product.dto.request.SkuUpdateRequest;
import com.legallyshop.legallyshop.product.dto.request.StockAdjustRequest;
import com.legallyshop.legallyshop.product.dto.response.ProductDetailResponse;
import com.legallyshop.legallyshop.product.entity.Product;
import com.legallyshop.legallyshop.product.entity.Sku;
import com.legallyshop.legallyshop.product.entity.SkuOption;
import com.legallyshop.legallyshop.product.repository.ProductRepository;
import com.legallyshop.legallyshop.product.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SkuService {

    private final SkuRepository skuRepo;
    private final ProductRepository productRepo;

    // ════════════════════════════════════════════════════════════════════════
    // Admin — SKU CRUD
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách SKU của một product (bao gồm cả inactive).
     */
    public List<ProductDetailResponse.SkuResponse> getSkusByProduct(Long productId) {
        findProductById(productId); // validate product tồn tại
        return skuRepo.findByProductId(productId).stream()
                .map(this::toSkuResponse)
                .toList();
    }

    @Transactional
    public ProductDetailResponse.SkuResponse addSku(Long productId, SkuCreateRequest req) {
        Product product = findProductById(productId);

        // Kiểm tra trùng skuCode trong toàn hệ thống
        if (skuRepo.existsBySkuCode(req.getSkuCode())) {
            throw AppException.badRequest("Mã SKU '" + req.getSkuCode() + "' đã tồn tại");
        }

        Sku sku = new Sku();
        sku.setProduct(product);
        sku.setSkuCode(req.getSkuCode());
        sku.setPrice(req.getPrice());
        sku.setOriginalPrice(req.getOriginalPrice());
        sku.setStockQty(req.getStockQty() == null ? 0 : req.getStockQty());

        for (var o : req.getOptions()) {
            SkuOption opt = new SkuOption();
            opt.setSku(sku);
            opt.setOptionName(o.getOptionName());
            opt.setOptionValue(o.getOptionValue());
            sku.getOptions().add(opt);
        }

        return toSkuResponse(skuRepo.save(sku));
    }

    @Transactional
    public ProductDetailResponse.SkuResponse updateSku(Long productId, Long skuId,
                                                       SkuUpdateRequest req) {
        Sku sku = findSkuByIdAndProduct(skuId, productId);

        if (req.getPrice() != null) sku.setPrice(req.getPrice());
        if (req.getIsActive() != null) sku.setIsActive(req.getIsActive());
        // originalPrice dùng set trực tiếp — null có nghĩa là xóa giảm giá
        sku.setOriginalPrice(req.getOriginalPrice());

        return toSkuResponse(skuRepo.save(sku));
    }

    @Transactional
    public void deleteSku(Long productId, Long skuId) {
        Sku sku = findSkuByIdAndProduct(skuId, productId);

        // Không cho xóa nếu đây là SKU duy nhất của product
        long activeSkuCount = skuRepo.countByProductIdAndIsActiveTrue(productId);
        if (activeSkuCount <= 1 && Boolean.TRUE.equals(sku.getIsActive())) {
            throw AppException.badRequest(
                    "Không thể xóa SKU cuối cùng. Sản phẩm cần ít nhất 1 SKU active.");
        }

        skuRepo.delete(sku);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Stock Management
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Điều chỉnh tồn kho thủ công.
     * delta > 0: nhập hàng | delta < 0: xuất kho / hàng hỏng
     */
    @Transactional
    public ProductDetailResponse.SkuResponse adjustStock(Long productId, Long skuId,
                                                         StockAdjustRequest req) {
        Sku sku = findSkuByIdAndProduct(skuId, productId);

        int newQty = sku.getStockQty() + req.getDelta();
        if (newQty < 0) {
            throw AppException.badRequest(
                    "Tồn kho không đủ. Hiện tại: " + sku.getStockQty()
                            + ", điều chỉnh: " + req.getDelta());
        }

        sku.setStockQty(newQty);
        log.info("Stock adjusted — SKU {}: {} → {} (reason: {})",
                sku.getSkuCode(), sku.getStockQty() - req.getDelta(),
                newQty, req.getReason());

        return toSkuResponse(skuRepo.save(sku));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Internal — dùng bởi OrderService
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public void decreaseStock(Long skuId, int quantity) {
        Sku sku = skuRepo.findById(skuId)
                .orElseThrow(() -> AppException.notFound("SKU"));
        if (sku.getStockQty() < quantity) {
            throw AppException.badRequest(
                    "Không đủ hàng. Tồn kho hiện tại: " + sku.getStockQty());
        }
        sku.setStockQty(sku.getStockQty() - quantity);
    }

    @Transactional
    public void increaseStock(Long skuId, int quantity) {
        Sku sku = skuRepo.findById(skuId)
                .orElseThrow(() -> AppException.notFound("SKU"));
        sku.setStockQty(sku.getStockQty() + quantity);
    }

    public Sku getAvailable(Long skuId) {
        return skuRepo.findAvailableSku(skuId)
                .orElseThrow(() -> AppException.badRequest(
                        "SKU không tồn tại hoặc đã hết hàng"));
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════════════════════════════

    private Product findProductById(Long productId) {
        return productRepo.findById(productId)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
    }

    private Sku findSkuByIdAndProduct(Long skuId, Long productId) {
        Sku sku = skuRepo.findById(skuId)
                .orElseThrow(() -> AppException.notFound("SKU"));
        if (!sku.getProduct().getId().equals(productId)) {
            throw AppException.badRequest("SKU không thuộc sản phẩm này");
        }
        return sku;
    }

    private ProductDetailResponse.SkuResponse toSkuResponse(Sku s) {
        return ProductDetailResponse.SkuResponse.builder()
                .id(s.getId())
                .skuCode(s.getSkuCode())
                .price(s.getPrice())
                .originalPrice(s.getOriginalPrice())
                .stockQty(s.getStockQty())
                .isActive(s.getIsActive())
                .options(s.getOptions().stream().map(o ->
                        ProductDetailResponse.OptionResponse.builder()
                                .optionName(o.getOptionName())
                                .optionValue(o.getOptionValue())
                                .build()).toList())
                .build();
    }
}