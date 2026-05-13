package com.legallyshop.legallyshop.product.repository;

import com.legallyshop.legallyshop.product.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkuRepository extends JpaRepository<Sku, Long> {

    List<Sku> findByProductIdAndIsActiveTrue(Long productId);

    Optional<Sku> findBySkuCode(String skuCode);

    @Query("SELECT s FROM Sku s WHERE s.id = :id AND s.stockQty > 0 AND s.isActive = true")
    Optional<Sku> findAvailableSku(@Param("id") Long id);

    List<Sku> findByProductId(Long productId);

    boolean existsBySkuCode(String skuCode);

    boolean existsBySkuCodeAndIdNot(String skuCode, Long id);

    long countByProductIdAndIsActiveTrue(Long productId);
}
