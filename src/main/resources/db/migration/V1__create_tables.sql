CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS tb_categoria (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_categoria_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS tb_produto (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    category_id BIGINT NOT NULL,
    CONSTRAINT fk_produto_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_produto_categoria FOREIGN KEY (category_id) REFERENCES tb_categoria (id)
);

CREATE TABLE IF NOT EXISTS tb_debtors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    value NUMERIC(19, 2) NOT NULL CHECK (value >= 0),
    date TIMESTAMP,
    CONSTRAINT fk_debtors_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_categoria_user_id ON tb_categoria (user_id);
CREATE INDEX IF NOT EXISTS idx_produto_user_id ON tb_produto (user_id);
CREATE INDEX IF NOT EXISTS idx_produto_category_id ON tb_produto (category_id);
CREATE INDEX IF NOT EXISTS idx_debtors_user_id ON tb_debtors (user_id);
