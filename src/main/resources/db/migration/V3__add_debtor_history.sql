ALTER TABLE tb_debtors
    ADD COLUMN IF NOT EXISTS paid BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS settled_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS tb_debtor_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    debtor_id BIGINT,
    debtor_name VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    previous_balance NUMERIC(19, 2) NOT NULL,
    new_balance NUMERIC(19, 2) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_debtor_tx_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_debtor_tx_debtor FOREIGN KEY (debtor_id) REFERENCES tb_debtors (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_debtor_tx_user_id ON tb_debtor_transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_debtor_tx_debtor_id ON tb_debtor_transactions (debtor_id);
CREATE INDEX IF NOT EXISTS idx_debtors_user_paid ON tb_debtors (user_id, paid);

INSERT INTO tb_debtor_transactions (
    user_id,
    debtor_id,
    debtor_name,
    transaction_type,
    amount,
    previous_balance,
    new_balance,
    description,
    created_at
)
SELECT
    d.user_id,
    d.id,
    d.name,
    'LANCAMENTO',
    d.value,
    0.00,
    d.value,
    d.description,
    COALESCE(d.date, CURRENT_TIMESTAMP)
FROM tb_debtors d
WHERE NOT EXISTS (
    SELECT 1 FROM tb_debtor_transactions t WHERE t.debtor_id = d.id
);
