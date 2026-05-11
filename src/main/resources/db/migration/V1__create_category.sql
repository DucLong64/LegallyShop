-- V1__create_category_and_attribute.sql
CREATE TABLE category (
                          id          BIGSERIAL PRIMARY KEY,
                          parent_id   BIGINT REFERENCES category(id),
                          name        VARCHAR(255) NOT NULL,
                          slug        VARCHAR(255) NOT NULL UNIQUE,
                          sort_order  INT DEFAULT 0,
                          created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE attribute_template (
                                    id          BIGSERIAL PRIMARY KEY,
                                    category_id BIGINT NOT NULL REFERENCES category(id),
                                    name        VARCHAR(100) NOT NULL,
                                    input_type  VARCHAR(50)  NOT NULL CHECK (input_type IN ('text','select','number','boolean')),
                                    is_required BOOLEAN DEFAULT FALSE,
                                    sort_order  INT DEFAULT 0
);

-- Seed data: danh mục gốc
INSERT INTO category (name, slug) VALUES
                                      ('Điện thoại',    'dien-thoai'),
                                      ('Laptop',        'laptop'),
                                      ('Đồ thể thao',   'do-the-thao');

-- Attribute cho Điện thoại
INSERT INTO attribute_template (category_id, name, input_type, is_required, sort_order)
VALUES
    (1, 'RAM',         'select', true, 1),
    (1, 'ROM',         'select', true, 2),
    (1, 'Pin (mAh)',   'number', false, 3),
    (1, 'Hệ điều hành','select', true, 4);