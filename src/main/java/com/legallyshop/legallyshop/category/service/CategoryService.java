package com.legallyshop.legallyshop.category.service;

import com.legallyshop.legallyshop.category.dto.request.AttributeTemplateRequest;
import com.legallyshop.legallyshop.category.dto.request.CategoryRequest;
import com.legallyshop.legallyshop.category.dto.request.ReorderRequest;
import com.legallyshop.legallyshop.category.dto.response.AttributeTemplateResponse;
import com.legallyshop.legallyshop.category.dto.response.CategoryDetailResponse;
import com.legallyshop.legallyshop.category.dto.response.CategoryDropdownResponse;
import com.legallyshop.legallyshop.category.dto.response.CategoryTreeResponse;
import com.legallyshop.legallyshop.category.entity.AttributeTemplate;
import com.legallyshop.legallyshop.category.entity.Category;
import com.legallyshop.legallyshop.category.repository.AttributeTemplateRepository;
import com.legallyshop.legallyshop.category.repository.CategoryRepository;
import com.legallyshop.legallyshop.common.config.SlugUtils;
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

    // CATEGORY — Public

    /**
     * Cây danh mục đầy đủ (cha → con đệ quy). Dùng cho menu frontend.
     */
    public List<CategoryTreeResponse> getCategoryTree() {
        return categoryRepo.findByParentIsNullOrderBySortOrderAsc()
                .stream()
                .map(this::toTreeResponse)
                .toList();
    }

    /**
     * Lấy 1 danh mục theo slug. Dùng cho breadcrumb và SEO.
     */
    public CategoryDetailResponse getBySlug(String slug) {
        Category cat = categoryRepo.findBySlug(slug)
                .orElseThrow(() -> AppException.notFound("Danh mục '" + slug + "'"));
        return toDetail(cat);
    }


    // CATEGORY — Admin CRUD

    /**
     * Lấy chi tiết danh mục theo ID — dùng cho form edit ở Admin.
     */
    public CategoryDetailResponse getById(Long id) {
        return toDetail(findCategoryById(id));
    }

    /**
     * Danh sách phẳng tất cả danh mục — dùng cho dropdown chọn danh mục cha.
     */
    public List<CategoryDropdownResponse> getAll() {
        List<Category> allCategories = categoryRepo.findAll();
        return allCategories.stream()
                .map(cat -> CategoryDropdownResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .slug(cat.getSlug())
                        .parentId(cat.getParent() != null ? cat.getParent().getId() : null)
                        .sortOrder(cat.getSortOrder())
                        .level(calculateLevel(cat))
                        .build())
                .toList();
    }

    private int calculateLevel(Category cat) {
        int level = 0;
        Category current = cat;
        while (current.getParent() != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    @Transactional
    public CategoryDetailResponse create(CategoryRequest req) {
        // Validate slug
        String slug = resolveSlug(req.getSlug(), req.getName());
        if (categoryRepo.existsBySlug(slug)) {
            throw AppException.badRequest("Slug '" + slug + "' đã tồn tại");
        }

        Category cat = new Category();
        cat.setName(req.getName());
        cat.setSlug(slug);
        cat.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());

        if (req.getParentId() != null) {
            Category parent = findCategoryById(req.getParentId());
            cat.setParent(parent);
        }

        return toDetail(categoryRepo.save(cat));
    }

    @Transactional
    public CategoryDetailResponse update(Long id, CategoryRequest req) {
        Category cat = findCategoryById(id);

        // Validate slug
        String slug = resolveSlug(req.getSlug(), req.getName());
        if (categoryRepo.existsBySlugAndIdNot(slug, id)) {
            throw AppException.badRequest("Slug '" + slug + "' đã tồn tại");
        }

        // Không cho phép đặt chính nó hoặc con của nó làm cha
        if (req.getParentId() != null) {
            if (req.getParentId().equals(id)) {
                throw AppException.badRequest("Danh mục không thể là cha của chính nó");
            }
            Category newParent = findCategoryById(req.getParentId());
            if (isDescendant(newParent, id)) {
                throw AppException.badRequest(
                        "Không thể đặt danh mục con làm cha của danh mục hiện tại");
            }
            cat.setParent(newParent);
        } else {
            cat.setParent(null); // Nâng lên thành danh mục gốc
        }

        cat.setName(req.getName());
        cat.setSlug(slug);
        cat.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());

        return toDetail(categoryRepo.save(cat));
    }

    @Transactional
    public void delete(Long id) {
        Category cat = findCategoryById(id);

        if (categoryRepo.existsByParentId(id)) {
            throw AppException.badRequest(
                    "Không thể xóa danh mục đang có danh mục con. Xóa danh mục con trước.");
        }
        if (categoryRepo.hasActiveProducts(id)) {
            throw AppException.badRequest(
                    "Không thể xóa danh mục đang có sản phẩm. Chuyển sản phẩm sang danh mục khác trước.");
        }

        categoryRepo.delete(cat);
    }

    /**
     * Đổi thứ tự các danh mục con (drag & drop ở Admin).
     * Nhận danh sách id theo thứ tự mới, tự gán sortOrder = index.
     */
    @Transactional
    public void reorder(ReorderRequest req) {
        List<Long> ids = req.getIds();
        for (int i = 0; i < ids.size(); i++) {
            final int order = i;
            categoryRepo.findById(ids.get(i)).ifPresent(c -> c.setSortOrder(order));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ATTRIBUTE TEMPLATE — Public
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Lấy danh sách attribute template theo danh mục — dùng khi tạo sản phẩm.
     */
    public List<AttributeTemplateResponse> getAttributeTemplates(Long categoryId) {
        if (!categoryRepo.existsById(categoryId)) {
            throw AppException.notFound("Danh mục");
        }
        return templateRepo.findByCategoryIdOrderBySortOrderAsc(categoryId)
                .stream()
                .map(this::toTemplateResponse)
                .toList();
    }

    // ════════════════════════════════════════════════════════════════════════
    // ATTRIBUTE TEMPLATE — Admin CRUD
    // ════════════════════════════════════════════════════════════════════════

    @Transactional
    public AttributeTemplateResponse createTemplate(Long categoryId,
                                                    AttributeTemplateRequest req) {
        Category cat = findCategoryById(categoryId);

        if (templateRepo.existsByCategoryIdAndName(categoryId, req.getName())) {
            throw AppException.badRequest(
                    "Thuộc tính '" + req.getName() + "' đã tồn tại trong danh mục này");
        }

        AttributeTemplate tmpl = new AttributeTemplate();
        tmpl.setCategory(cat);
        tmpl.setName(req.getName());
        tmpl.setInputType(req.getInputType());
        tmpl.setIsRequired(Boolean.TRUE.equals(req.getIsRequired()));
        tmpl.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());

        return toTemplateResponse(templateRepo.save(tmpl));
    }

    @Transactional
    public AttributeTemplateResponse updateTemplate(Long categoryId, Long templateId,
                                                    AttributeTemplateRequest req) {
        findCategoryById(categoryId); // validate category tồn tại

        AttributeTemplate tmpl = templateRepo.findById(templateId)
                .orElseThrow(() -> AppException.notFound("Attribute template"));

        if (!tmpl.getCategory().getId().equals(categoryId)) {
            throw AppException.badRequest("Template không thuộc danh mục này");
        }

        if (templateRepo.existsByCategoryIdAndNameAndIdNot(categoryId, req.getName(), templateId)) {
            throw AppException.badRequest(
                    "Thuộc tính '" + req.getName() + "' đã tồn tại trong danh mục này");
        }

        tmpl.setName(req.getName());
        tmpl.setInputType(req.getInputType());
        tmpl.setIsRequired(Boolean.TRUE.equals(req.getIsRequired()));
        tmpl.setSortOrder(req.getSortOrder() == null ? 0 : req.getSortOrder());

        return toTemplateResponse(templateRepo.save(tmpl));
    }

    @Transactional
    public void deleteTemplate(Long categoryId, Long templateId) {
        findCategoryById(categoryId);

        AttributeTemplate tmpl = templateRepo.findById(templateId)
                .orElseThrow(() -> AppException.notFound("Attribute template"));

        if (!tmpl.getCategory().getId().equals(categoryId)) {
            throw AppException.badRequest("Template không thuộc danh mục này");
        }

        templateRepo.delete(tmpl);
    }

    /**
     * Đổi thứ tự attribute template trong danh mục.
     */
    @Transactional
    public void reorderTemplates(Long categoryId, ReorderRequest req) {
        findCategoryById(categoryId);
        List<Long> ids = req.getIds();
        for (int i = 0; i < ids.size(); i++) {
            final int order = i;
            templateRepo.findById(ids.get(i)).ifPresent(t -> t.setSortOrder(order));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Private helpers
    // ════════════════════════════════════════════════════════════════════════

    private Category findCategoryById(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> AppException.notFound("Danh mục"));
    }

    /**
     * Kiểm tra xem candidate có phải là hậu duệ của ancestorId không.
     */
    private boolean isDescendant(Category candidate, Long ancestorId) {
        Category current = candidate;
        while (current.getParent() != null) {
            if (current.getParent().getId().equals(ancestorId)) return true;
            current = current.getParent();
        }
        return false;
    }

    /**
     * Ưu tiên slug do người dùng nhập, nếu không có thì tự sinh từ name.
     */
    private String resolveSlug(String inputSlug, String name) {
        return (inputSlug != null && !inputSlug.isBlank())
                ? inputSlug.trim().toLowerCase()
                : SlugUtils.toSlug(name);
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private CategoryTreeResponse toTreeResponse(Category cat) {
        return CategoryTreeResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .children(cat.getChildren().stream()
                        .map(this::toTreeResponse).toList())
                .build();
    }

    private CategoryDetailResponse toDetail(Category cat) {
        return CategoryDetailResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .sortOrder(cat.getSortOrder())
                .parentId(cat.getParent() != null ? cat.getParent().getId() : null)
                .parentName(cat.getParent() != null ? cat.getParent().getName() : null)
                .children(cat.getChildren().stream()
                        .map(c -> CategoryDetailResponse.CategorySimpleResponse.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .slug(c.getSlug())
                                .sortOrder(c.getSortOrder())
                                .build())
                        .toList())
                .attributeTemplates(
                        templateRepo.findByCategoryIdOrderBySortOrderAsc(cat.getId())
                                .stream().map(this::toTemplateResponse).toList())
                .createdAt(cat.getCreatedAt())
                .build();
    }

    private AttributeTemplateResponse toTemplateResponse(AttributeTemplate t) {
        return AttributeTemplateResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .inputType(t.getInputType())
                .isRequired(t.getIsRequired())
                .sortOrder(t.getSortOrder())
                .build();
    }
}