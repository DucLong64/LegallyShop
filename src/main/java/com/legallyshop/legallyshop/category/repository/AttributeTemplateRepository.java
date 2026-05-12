package com.legallyshop.legallyshop.category.repository;

import com.legallyshop.legallyshop.category.entity.AttributeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AttributeTemplateRepository extends JpaRepository<AttributeTemplate, Long> {
    List<AttributeTemplate> findByCategoryIdOrderBySortOrderAsc(Long categoryId);

    // Kiểm tra tên template trùng trong cùng danh mục
    boolean existsByCategoryIdAndName(Long categoryId, String name);

    // Kiểm tra tên trùng khi update (loại trừ chính nó)
    boolean existsByCategoryIdAndNameAndIdNot(Long categoryId, String name, Long id);

    // Xóa toàn bộ template của một danh mục (dùng khi xóa danh mục)
    @Modifying
    @Query("DELETE FROM AttributeTemplate t WHERE t.category.id = :categoryId")
    void deleteByCategoryId(@Param("categoryId") Long categoryId);
}
