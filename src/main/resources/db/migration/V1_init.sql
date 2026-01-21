-- Flyway V1: Initial schema
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS categories (
                                          id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(120) NOT NULL,
    slug varchar(160) NOT NULL,
    CONSTRAINT uk_categories_slug UNIQUE (slug)
    );

CREATE TABLE IF NOT EXISTS products (
                                        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id uuid NULL,
    name varchar(200) NOT NULL,
    slug varchar(220) NOT NULL,
    description text NULL,
    base_price numeric(12,2) NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT uk_products_slug UNIQUE (slug),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
    );

CREATE TABLE IF NOT EXISTS product_variants (
                                                id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id uuid NOT NULL,
    sku_code varchar(80) NOT NULL,
    color varchar(40) NOT NULL,
    size varchar(16) NOT NULL,
    price numeric(12,2) NOT NULL,
    stock_on_hand int NOT NULL DEFAULT 0,
    stock_reserved int NOT NULL DEFAULT 0,
    is_active boolean NOT NULL DEFAULT true,
    version bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT fk_variants_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uk_variants_sku_code UNIQUE (sku_code)
    );

CREATE INDEX IF NOT EXISTS idx_variants_product ON product_variants(product_id);

CREATE TABLE IF NOT EXISTS carts (
                                     id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    token uuid NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT uk_carts_token UNIQUE (token)
    );

CREATE TABLE IF NOT EXISTS cart_items (
                                          id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id uuid NOT NULL,
    variant_id uuid NOT NULL,
    quantity int NOT NULL,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id),
    CONSTRAINT uk_cart_items_cart_variant UNIQUE (cart_id, variant_id)
    );

CREATE TABLE IF NOT EXISTS inventory_reservations (
                                                      id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    token uuid NOT NULL,
    cart_token uuid NOT NULL,
    status varchar(16) NOT NULL,
    expires_at timestamptz NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT uk_reservations_token UNIQUE (token)
    );

CREATE INDEX IF NOT EXISTS idx_reservations_status_expires ON inventory_reservations(status, expires_at);

CREATE TABLE IF NOT EXISTS inventory_reservation_items (
                                                           id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id uuid NOT NULL,
    variant_id uuid NOT NULL,
    quantity int NOT NULL,
    CONSTRAINT fk_res_items_reservation FOREIGN KEY (reservation_id) REFERENCES inventory_reservations(id) ON DELETE CASCADE,
    CONSTRAINT fk_res_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id)
    );

CREATE INDEX IF NOT EXISTS idx_res_items_reservation ON inventory_reservation_items(reservation_id);

CREATE TABLE IF NOT EXISTS orders (
                                      id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_code varchar(32) NOT NULL,
    tracking_token uuid NOT NULL,
    reservation_token uuid NOT NULL,
    email varchar(200) NOT NULL,
    full_name varchar(120) NOT NULL,
    phone varchar(30) NOT NULL,
    address_line1 varchar(220) NOT NULL,
    address_line2 varchar(220) NULL,
    city varchar(120) NOT NULL,
    payment_method varchar(20) NOT NULL,
    status varchar(20) NOT NULL,
    total_amount numeric(12,2) NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT uk_orders_order_code UNIQUE (order_code),
    CONSTRAINT uk_orders_tracking_token UNIQUE (tracking_token)
    );

CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at);

CREATE TABLE IF NOT EXISTS order_items (
                                           id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id uuid NOT NULL,
    variant_id uuid NOT NULL,
    sku_code varchar(80) NOT NULL,
    product_name varchar(200) NOT NULL,
    variant_name varchar(100) NOT NULL,
    unit_price numeric(12,2) NOT NULL,
    quantity int NOT NULL,
    line_total numeric(12,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);

