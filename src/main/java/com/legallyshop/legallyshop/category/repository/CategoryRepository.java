package com.legallyshop.legallyshop.category.repository;

import com.legallyshop.legallyshop.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Lấy danh mục gốc (không có cha)
    List<Category> findByParentIsNullOrderBySortOrderAsc();

    // Lấy danh mục con trực tiếp
    List<Category> findByParentIdOrderBySortOrderAsc(Long parentId);

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    // Kiểm tra slug trùng khi update (loại trừ chính nó)
    boolean existsBySlugAndIdNot(String slug, Long id);

    // Kiểm tra còn sản phẩm trong danh mục không (dùng trước khi xóa)
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    boolean hasActiveProducts(@Param("categoryId") Long categoryId);

    // Kiểm tra còn danh mục con không (dùng trước khi xóa)
    boolean existsByParentId(Long parentId);
}
