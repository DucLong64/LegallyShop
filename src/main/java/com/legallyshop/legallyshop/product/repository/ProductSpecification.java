package com.legallyshop.legallyshop.product.repository;

import com.legallyshop.legallyshop.product.dto.request.ProductSearchRequest;
import com.legallyshop.legallyshop.product.entity.Product;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    private ProductSpecification() {}

    /** Chỉ lấy sản phẩm isActive=true (áp dụng mặc định cho public API) */
    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }

    /** Lọc theo danh mục */
    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                cb.equal(root.get("category").get("id"), categoryId);
    }

    /** Tìm theo tên — case-insensitive */
    public static Specification<Product> nameContains(String keyword) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
    }

    /** Lọc giá tối thiểu — dựa trên giá thấp nhất trong các SKU active */
    public static Specification<Product> minPrice(BigDecimal min) {
        return (root, query, cb) -> {
            Join<?, ?> skuJoin = root.join("skus", JoinType.LEFT);
            query.distinct(true);
            return cb.and(
                    cb.isTrue(skuJoin.get("isActive")),
                    cb.greaterThanOrEqualTo(skuJoin.get("price"), min)
            );
        };
    }

    /** Lọc giá tối đa */
    public static Specification<Product> maxPrice(BigDecimal max) {
        return (root, query, cb) -> {
            Join<?, ?> skuJoin = root.join("skus", JoinType.LEFT);
            query.distinct(true);
            return cb.and(
                    cb.isTrue(skuJoin.get("isActive")),
                    cb.lessThanOrEqualTo(skuJoin.get("price"), max)
            );
        };
    }

    /** Lọc theo khoảng giá (gộp min + max, dùng 1 join duy nhất) */
    public static Specification<Product> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            Join<?, ?> skuJoin = root.join("skus", JoinType.LEFT);
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(skuJoin.get("isActive")));
            if (min != null) predicates.add(cb.greaterThanOrEqualTo(skuJoin.get("price"), min));
            if (max != null) predicates.add(cb.lessThanOrEqualTo(skuJoin.get("price"), max));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** Chỉ lấy sản phẩm còn hàng (ít nhất 1 SKU có stockQty > 0) */
    public static Specification<Product> inStock() {
        return (root, query, cb) -> {
            Join<?, ?> skuJoin = root.join("skus", JoinType.LEFT);
            query.distinct(true);
            return cb.and(
                    cb.isTrue(skuJoin.get("isActive")),
                    cb.greaterThan(skuJoin.get("stockQty"), 0)
            );
        };
    }

    /**
     * Lọc theo attribute — sản phẩm phải có attribute với templateId và value tương ứng.
     * Gọi nhiều lần để AND nhiều điều kiện.
     * VD: hasAttribute(1, "8GB") AND hasAttribute(3, "iOS")
     */
    public static Specification<Product> hasAttribute(Long templateId, String value) {
        return (root, query, cb) -> {
            // EXISTS (SELECT 1 FROM product_attribute pa
            //         WHERE pa.product = product
            //         AND pa.template.id = templateId
            //         AND pa.value = value)
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<?> attrRoot = subquery.correlate(root);
            Join<?, ?> attrJoin = attrRoot.join("attributes");
            subquery.select(cb.literal(1L))
                    .where(
                            cb.equal(attrJoin.get("template").get("id"), templateId),
                            cb.equal(cb.lower(attrJoin.get("value")), value.toLowerCase())
                    );
            return cb.exists(subquery);
        };
    }

    /**
     * Build Specification tổng hợp từ ProductSearchRequest.
     * Đây là entry point chính — gọi method này trong Service.
     */
    public static Specification<Product> fromRequest(ProductSearchRequest req,
                                                     boolean activeOnly) {
        Specification<Product> spec = Specification.unrestricted();

        if (activeOnly) {
            spec = spec.and(isActive());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            spec = spec.and(nameContains(req.getKeyword().trim()));
        }
        if (req.getCategoryId() != null) {
            spec = spec.and(hasCategory(req.getCategoryId()));
        }
        // Khoảng giá — gộp thành 1 join để tránh cartesian product
        if (req.getMinPrice() != null || req.getMaxPrice() != null) {
            spec = spec.and(priceBetween(req.getMinPrice(), req.getMaxPrice()));
        }
        if (Boolean.TRUE.equals(req.getInStockOnly())) {
            spec = spec.and(inStock());
        }
        // Attribute filters — mỗi filter AND với nhau
        if (req.getAttributes() != null) {
            for (var attr : req.getAttributes()) {
                if (attr.getTemplateId() != null && attr.getValue() != null) {
                    spec = spec.and(hasAttribute(attr.getTemplateId(), attr.getValue()));
                }
            }
        }
        return spec;
    }
}

