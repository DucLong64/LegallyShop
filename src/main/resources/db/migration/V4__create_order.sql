-- V4__create_order.sql
CREATE TABLE orders (
                        id               BIGSERIAL PRIMARY KEY,
                        order_code       VARCHAR(50) NOT NULL UNIQUE,
                        user_id          BIGINT REFERENCES users(id),
                        status           VARCHAR(50) DEFAULT 'PENDING'
                            CHECK (status IN
                                   ('PENDING','CONFIRMED','SHIPPING',
                                    'DELIVERED','CANCELLED')),
                        total_amount     NUMERIC(15,2) NOT NULL,
                        shipping_fee     NUMERIC(15,2) DEFAULT 0,
                        discount_amount  NUMERIC(15,2) DEFAULT 0,
                        receiver_name    VARCHAR(255),
                        receiver_phone   VARCHAR(20),
                        shipping_address TEXT,
                        payment_method   VARCHAR(50),  -- COD | VNPAY | MOMO
                        payment_status   VARCHAR(50) DEFAULT 'UNPAID',
                        note             TEXT,
                        created_at       TIMESTAMP DEFAULT NOW(),
                        updated_at       TIMESTAMP DEFAULT NOW()
);

CREATE TABLE order_item (
                            id           BIGSERIAL PRIMARY KEY,
                            order_id     BIGINT NOT NULL REFERENCES orders(id),
                            sku_id       BIGINT NOT NULL REFERENCES sku(id),
                            product_name VARCHAR(500),   -- snapshot
                            sku_options  TEXT,           -- snapshot: "Đen / 256GB"
                            quantity     INT NOT NULL CHECK (quantity > 0),
                            unit_price   NUMERIC(15,2) NOT NULL,
                            subtotal     NUMERIC(15,2) NOT NULL
);

CREATE INDEX idx_order_user   ON orders(user_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_code   ON orders(order_code);