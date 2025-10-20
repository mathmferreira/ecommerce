CREATE TABLE IF NOT EXISTS user_tb (
    user_id BINARY(16) NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS product_tb (
    product_id BINARY(16) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(19,2) NOT NULL,
    category VARCHAR(255) NOT NULL,
    stock_quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6)
);

CREATE TABLE IF NOT EXISTS order_tb (
    id BINARY(16) NOT NULL PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    FOREIGN KEY (user_id) REFERENCES user_tb(user_id)
);

CREATE TABLE IF NOT EXISTS order_item_tb (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    order_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    total_price DECIMAL(19,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES order_tb(id),
    FOREIGN KEY (product_id) REFERENCES product_tb(product_id)
);

CREATE INDEX idx_user_email ON user_tb(email);
CREATE INDEX idx_product_category ON product_tb(category);
CREATE INDEX idx_order_user_id ON order_tb(user_id);
CREATE INDEX idx_order_status ON order_tb(status);