CREATE TABLE IF NOT EXISTS user_profiles (
                                             id          BIGSERIAL PRIMARY KEY,
                                             user_id     BIGINT NOT NULL UNIQUE,
                                             email       VARCHAR(150) NOT NULL UNIQUE,
                                             username    VARCHAR(100) NOT NULL,
                                             full_name   VARCHAR(200),
                                             phone       VARCHAR(20),
                                             address     TEXT,
                                             created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_profiles_email   ON user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);