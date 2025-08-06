CREATE TYPE role_name_enum AS ENUM ('admin', 'teacher','user');

CREATE TABLE roles (
                       role_id SERIAL PRIMARY KEY,
                       name role_name_enum NOT NULL UNIQUE,
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
