package com.legallyshop.legallyshop.category.service;

import com.legallyshop.legallyshop.category.dto.response.AttributeTemplateResponse;
import com.legallyshop.legallyshop.category.dto.response.CategoryTreeResponse;
import com.legallyshop.legallyshop.category.entity.Category;
import com.legallyshop.legallyshop.category.repository.AttributeTemplateRepository;
import com.legallyshop.legallyshop.category.repository.CategoryRepository;
import com.legallyshop.legallyshop.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepo;
    private final AttributeTemplateRepository templateRepo;

    public List<CategoryTreeResponse> getCategoryTree() {
        return categoryRepo.findByParentIsNullOrderBySortOrderAsc()
                .stream()
                .map(this::toTreeResponse)
                .toList();
    }

    public List<AttributeTemplateResponse> getAttributeTemplates(Long categoryId) {
        if (!categoryRepo.existsById(categoryId)) {
            throw AppException.notFound("Danh mục");
        }
        return templateRepo.findByCategoryIdOrderBySortOrderAsc(categoryId)
                .stream()
                .map(t -> AttributeTemplateResponse.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .inputType(t.getInputType())
                        .isRequired(t.getIsRequired())
                        .sortOrder(t.getSortOrder())
                        .build())
                .toList();
    }

    private CategoryTreeResponse toTreeResponse(Category cat) {
        return CategoryTreeResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .children(cat.getChildren().stream()
                        .map(this::toTreeResponse).toList())
                .build();
    }
}