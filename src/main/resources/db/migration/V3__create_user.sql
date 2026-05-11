-- V3__create_user.sql
CREATE TABLE users (
                       id         BIGSERIAL PRIMARY KEY,
                       email      VARCHAR(255) NOT NULL UNIQUE,
                       password   VARCHAR(255) NOT NULL,
                       full_name  VARCHAR(255),
                       phone      VARCHAR(20),
                       role       VARCHAR(50) DEFAULT 'CUSTOMER'
                           CHECK (role IN ('CUSTOMER', 'ADMIN')),
                       is_active  BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE user_address (
                              id          BIGSERIAL PRIMARY KEY,
                              user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              full_name   VARCHAR(255) NOT NULL,
                              phone       VARCHAR(20)  NOT NULL,
                              province    VARCHAR(100),
                              district    VARCHAR(100),
                              ward        VARCHAR(100),
                              detail      TEXT,
                              is_default  BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_user_email ON users(email);