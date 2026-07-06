CREATE TABLE IF NOT EXISTS roles (
                                     id      BIGSERIAL PRIMARY KEY,
                                     name    VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
                                     id          BIGSERIAL PRIMARY KEY,
                                     username    VARCHAR(100) NOT NULL UNIQUE,
                                     email       VARCHAR(150) NOT NULL UNIQUE,
                                     password    VARCHAR(255) NOT NULL,
                                     role_id     BIGINT NOT NULL,
                                     created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE INDEX IF NOT EXISTS idx_users_email    ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

INSERT INTO roles(name) VALUES ('ROLE_USER')  ON CONFLICT DO NOTHING;
INSERT INTO roles(name) VALUES ('ROLE_ADMIN') ON CONFLICT DO NOTHING;