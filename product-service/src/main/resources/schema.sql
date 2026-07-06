CREATE TABLE IF NOT EXISTS categories (
                                          id          BIGSERIAL PRIMARY KEY,
                                          name        VARCHAR(100) NOT NULL UNIQUE,
                                          description TEXT
);

CREATE TABLE IF NOT EXISTS products (
                                        id          BIGSERIAL PRIMARY KEY,
                                        name        VARCHAR(200) NOT NULL,
                                        description TEXT,
                                        price       NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
                                        stock       INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
                                        category_id BIGINT NOT NULL,
                                        image_url   VARCHAR(500),
                                        created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_name        ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_price       ON products(price);

INSERT INTO categories(name, description) VALUES
    ('Electronics',  'Electronic devices and accessories') ON CONFLICT DO NOTHING;
INSERT INTO categories(name, description) VALUES
    ('Clothing',     'Apparel and fashion items')          ON CONFLICT DO NOTHING;
INSERT INTO categories(name, description) VALUES
    ('Books',        'Physical and digital books')         ON CONFLICT DO NOTHING;
INSERT INTO categories(name, description) VALUES
    ('Home & Garden','Home improvement and garden tools')  ON CONFLICT DO NOTHING;