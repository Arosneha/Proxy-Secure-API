CREATE TABLE api_usage (
    id BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL UNIQUE,
    request_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_api_usage_request_count
        CHECK (request_count >= 0)
);