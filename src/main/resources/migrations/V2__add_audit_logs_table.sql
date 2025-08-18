CREATE TABLE audit_logs (
                            id SERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            entity_type VARCHAR(50) NOT NULL,
                            entity_id BIGINT NOT NULL,
                            action VARCHAR(10) NOT NULL CHECK (action IN ('CREATE','UPDATE','DELETE')),
    changes JSONB NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_user ON audit_logs (user_id);
