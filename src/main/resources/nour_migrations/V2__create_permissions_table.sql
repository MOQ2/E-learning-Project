CREATE TABLE permissions (
                             permission_id SERIAL PRIMARY KEY,
                             permission_name VARCHAR(100) NOT NULL UNIQUE,
                             created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
