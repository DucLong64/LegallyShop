package com.legallyshop.legallyshop.product.repository;

import com.legallyshop.legallyshop.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    Optional<ProductImage> findByIdAndProductId(Long id, Long productId);

    // Reset tất cả ảnh của product về isPrimary=false trước khi set ảnh mới
    @Modifying
    @Query("UPDATE ProductImage i SET i.isPrimary = false WHERE i.product.id = :productId")
    void clearPrimaryByProductId(@Param("productId") Long productId);
}
