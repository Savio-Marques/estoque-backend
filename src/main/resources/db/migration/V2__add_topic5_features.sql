ALTER TABLE tb_produto
    ADD COLUMN IF NOT EXISTS cost_price NUMERIC(19, 2) NOT NULL DEFAULT 0.00 CHECK (cost_price >= 0),
    ADD COLUMN IF NOT EXISTS sale_price NUMERIC(19, 2) NOT NULL DEFAULT 0.00 CHECK (sale_price >= 0),
    ADD COLUMN IF NOT EXISTS min_quantity INTEGER NOT NULL DEFAULT 5 CHECK (min_quantity >= 0);

CREATE TABLE IF NOT EXISTS tb_stock_movements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    quantity_delta INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_movement_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_movement_user_id ON tb_stock_movements (user_id);
