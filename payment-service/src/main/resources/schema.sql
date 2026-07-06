CREATE TABLE IF NOT EXISTS payments (
    id                 BIGSERIAL PRIMARY KEY,
    order_id           BIGINT NOT NULL UNIQUE,
    user_id            BIGINT NOT NULL,
    amount             NUMERIC(10,2) NOT NULL,
    status             VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                           CHECK (status IN ('PENDING','COMPLETED','FAILED','REFUNDED')),
    stripe_session_id  VARCHAR(255),
    stripe_payment_id  VARCHAR(255),
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id         ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id          ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_stripe_session   ON payments(stripe_session_id);
