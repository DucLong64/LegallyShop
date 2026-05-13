package com.legallyshop.legallyshop.product.service;

import com.legallyshop.legallyshop.category.entity.AttributeTemplate;
import com.legallyshop.legallyshop.category.repository.AttributeTemplateRepository;
import com.legallyshop.legallyshop.category.repository.CategoryRepository;
import com.legallyshop.legallyshop.common.config.SlugUtils;
import com.legallyshop.legallyshop.common.exception.AppException;
import com.legallyshop.legallyshop.product.dto.request.ProductCreateRequest;
import com.legallyshop.legallyshop.product.dto.request.ProductSearchRequest;
import com.legallyshop.legallyshop.product.dto.request.ProductUpdateRequest;
import com.legallyshop.legallyshop.product.dto.response.ProductDetailResponse;
import com.legallyshop.legallyshop.product.dto.response.ProductSummaryResponse;
import com.legallyshop.legallyshop.product.entity.*;
import com.legallyshop.legallyshop.product.repository.ProductRepository;
import com.legallyshop.legallyshop.product.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository           productRepo;
    private final CategoryRepository          categoryRepo;
    private final AttributeTemplateRepository templateRepo;

    // ─── Queries ────────────────────────────────────────────────────────────

    /**
     * Search đơn giản — giữ lại cho backward-compatible.
     * Frontend mới nên dùng search() với ProductSearchRequest.
     */
    public Page<ProductSummaryResponse> findAll(Long categoryId, String keyword,
                                                Pageable pageable) {
        ProductSearchRequest req = new ProductSearchRequest();
        req.setCategoryId(categoryId);
        req.setKeyword(keyword);
        return search(req, pageable);
    }

    /**
     * Search nâng cao — filter theo giá, attribute, tồn kho.
     * Luôn chỉ lấy sản phẩm isActive=true (public API).
     */
    public Page<ProductSummaryResponse> search(ProductSearchRequest req, Pageable pageable) {
        return productRepo.findAll(
                ProductSpecification.fromRequest(req, true), pageable
        ).map(this::toSummary);
    }

    public ProductDetailResponse findBySlug(String slug) {
        return productRepo.findBySlugAndIsActiveTrue(slug)
                .map(this::toDetail)
                .orElseThrow(() -> AppException.notFound("Sản phẩm '" + slug + "'"));
    }

    public ProductDetailResponse findById(Long id) {
        return productRepo.findById(id)
                .map(this::toDetail)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
    }

    // ─── Commands ───────────────────────────────────────────────────────────

    @Transactional
    public ProductDetailResponse create(ProductCreateRequest req) {
        var category = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> AppException.notFound("Danh mục"));

        Product product = new Product();
        product.setCategory(category);
        product.setName(req.getName());
        product.setSlug(uniqueSlug(req.getName(), null));
        product.setDescription(req.getDescription());
        product.setStatus(ProductStatus.DRAFT);

        // Attributes
        for (var a : req.getAttributes()) {
            AttributeTemplate tmpl = templateRepo.findById(a.getTemplateId())
                    .orElseThrow(() -> AppException.notFound("Attribute template"));
            ProductAttribute attr = new ProductAttribute();
            attr.setProduct(product);
            attr.setTemplate(tmpl);
            attr.setValue(a.getValue());
            product.getAttributes().add(attr);
        }

        // SKUs
        for (var s : req.getSkus()) {
            Sku sku = new Sku();
            sku.setProduct(product);
            sku.setSkuCode(s.getSkuCode());
            sku.setPrice(s.getPrice());
            sku.setOriginalPrice(s.getOriginalPrice());
            sku.setStockQty(s.getStockQty() == null ? 0 : s.getStockQty());
            for (var o : s.getOptions()) {
                SkuOption opt = new SkuOption();
                opt.setSku(sku);
                opt.setOptionName(o.getOptionName());
                opt.setOptionValue(o.getOptionValue());
                sku.getOptions().add(opt);
            }
            product.getSkus().add(sku);
        }

        // Images
        for (var img : req.getImages()) {
            ProductImage pi = new ProductImage();
            pi.setProduct(product);
            pi.setUrl(img.getUrl());
            pi.setIsPrimary(Boolean.TRUE.equals(img.getIsPrimary()));
            pi.setSortOrder(img.getSortOrder() == null ? 0 : img.getSortOrder());
            product.getImages().add(pi);
        }

        return toDetail(productRepo.save(product));
    }

    @Transactional
    public ProductDetailResponse update(Long id, ProductUpdateRequest req) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));

        product.setName(req.getName());
        product.setSlug(uniqueSlug(req.getName(), id));
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getStatus()      != null) product.setStatus(req.getStatus());
        if (req.getCategoryId()  != null) {
            var cat = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> AppException.notFound("Danh mục"));
            product.setCategory(cat);
        }

        // Cập nhật attributes nếu có truyền (replace toàn bộ)
        if (req.getAttributes() != null && !req.getAttributes().isEmpty()) {
            product.getAttributes().clear();
            for (var a : req.getAttributes()) {
                AttributeTemplate tmpl = templateRepo.findById(a.getTemplateId())
                        .orElseThrow(() -> AppException.notFound("Attribute template"));
                ProductAttribute attr = new ProductAttribute();
                attr.setProduct(product);
                attr.setTemplate(tmpl);
                attr.setValue(a.getValue());
                product.getAttributes().add(attr);
            }
        }

        return toDetail(productRepo.save(product));
    }

    /** Publish / Unpublish nhanh — không cần gửi toàn bộ body. */
    @Transactional
    public ProductDetailResponse updateStatus(Long id, ProductStatus status) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        product.setStatus(status);
        return toDetail(productRepo.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Sản phẩm"));
        product.setIsActive(false);   // soft delete
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private String uniqueSlug(String name, Long excludeId) {
        String base = SlugUtils.toSlug(name);
        String slug = base;
        int    i    = 1;
        while (excludeId == null
                ? productRepo.existsBySlug(slug)
                : productRepo.existsBySlugAndIdNot(slug, excludeId)) {
            slug = base + "-" + i++;
        }
        return slug;
    }

    private ProductSummaryResponse toSummary(Product p) {
        return ProductSummaryResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .minPrice(p.getMinPrice())
                .imageUrl(p.getPrimaryImageUrl())
                .status(p.getStatus().name())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private ProductDetailResponse toDetail(Product p) {
        return ProductDetailResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .slug(p.getSlug())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .description(p.getDescription())
                .status(p.getStatus().name())
                .skus(p.getSkus().stream().map(s ->
                        ProductDetailResponse.SkuResponse.builder()
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
                                .build()).toList())
                .attributes(p.getAttributes().stream().map(a ->
                        ProductDetailResponse.AttributeResponse.builder()
                                .templateId(a.getTemplate().getId())
                                .templateName(a.getTemplate().getName())
                                .inputType(a.getTemplate().getInputType())
                                .value(a.getValue())
                                .build()).toList())
                .images(p.getImages().stream().map(i ->
                        ProductDetailResponse.ImageResponse.builder()
                                .id(i.getId())
                                .url(i.getUrl())
                                .isPrimary(i.getIsPrimary())
                                .sortOrder(i.getSortOrder())
                                .build()).toList())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

}
