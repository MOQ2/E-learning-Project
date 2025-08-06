CREATE TABLE users (
                       user_id SERIAL PRIMARY KEY,
                       name VARCHAR(250) NOT NULL,
                       email VARCHAR(250) NOT NULL UNIQUE,
                       phone VARCHAR(250) NOT NULL,
                       password VARCHAR(250) NOT NULL,
                       role_id INTEGER NOT NULL,
                       is_active BOOLEAN NOT NULL,
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(role_id)

);
