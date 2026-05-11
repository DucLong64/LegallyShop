-- V2__create_product_and_sku.sql
CREATE TABLE product (
                         id          BIGSERIAL PRIMARY KEY,
                         category_id BIGINT NOT NULL REFERENCES category(id),
                         name        VARCHAR(500) NOT NULL,
                         slug        VARCHAR(500) NOT NULL UNIQUE,
                         description TEXT,
                         status      VARCHAR(50) DEFAULT 'DRAFT'
                             CHECK (status IN ('DRAFT','ACTIVE','INACTIVE')),
                         is_active   BOOLEAN DEFAULT TRUE,
                         created_at  TIMESTAMP DEFAULT NOW(),
                         updated_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE product_attribute (
                                   id          BIGSERIAL PRIMARY KEY,
                                   product_id  BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
                                   template_id BIGINT NOT NULL REFERENCES attribute_template(id),
                                   value       TEXT NOT NULL
);

CREATE TABLE sku (
                     id             BIGSERIAL PRIMARY KEY,
                     product_id     BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
                     sku_code       VARCHAR(100) NOT NULL UNIQUE,
                     price          NUMERIC(15,2) NOT NULL,
                     original_price NUMERIC(15,2),
                     stock_qty      INT NOT NULL DEFAULT 0 CHECK (stock_qty >= 0),
                     is_active      BOOLEAN DEFAULT TRUE
);

CREATE TABLE sku_option (
                            id           BIGSERIAL PRIMARY KEY,
                            sku_id       BIGINT NOT NULL REFERENCES sku(id) ON DELETE CASCADE,
                            option_name  VARCHAR(100) NOT NULL,
                            option_value VARCHAR(100) NOT NULL
);

CREATE TABLE product_image (
                               id         BIGSERIAL PRIMARY KEY,
                               product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
                               sku_id     BIGINT REFERENCES sku(id),
                               url        VARCHAR(1000) NOT NULL,
                               is_primary BOOLEAN DEFAULT FALSE,
                               sort_order INT DEFAULT 0
);

-- Indexes
CREATE INDEX idx_product_category ON product(category_id);
CREATE INDEX idx_product_status   ON product(status, is_active);
CREATE INDEX idx_sku_product       ON sku(product_id);