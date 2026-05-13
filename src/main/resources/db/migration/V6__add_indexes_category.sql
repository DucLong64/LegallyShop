-- V6__add_category_indexes.sql
-- Bổ sung index hỗ trợ các query mới của category feature

-- Tìm danh mục con theo parent (dùng trong existsByParentId, findByParentId)
CREATE INDEX IF NOT EXISTS idx_category_parent ON category(parent_id);

-- Tìm danh mục theo sort_order (dùng trong danh sách Admin)
CREATE INDEX IF NOT EXISTS idx_category_sort ON category(sort_order);

-- Tìm attribute template theo category + sort_order
CREATE INDEX IF NOT EXISTS idx_attr_tmpl_category_sort
    ON attribute_template(category_id, sort_order);

-- Tìm attribute template theo tên trong cùng danh mục (check trùng)
CREATE INDEX IF NOT EXISTS idx_attr_tmpl_category_name
    ON attribute_template(category_id, name);