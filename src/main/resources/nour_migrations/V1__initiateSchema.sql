CREATE TABLE  roles (
                                     id SERIAL PRIMARY KEY,
                                     name VARCHAR(100) NOT NULL UNIQUE,
                                     created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
    );
INSERT INTO roles (name) VALUES
                             ('ADMIN'),
                             ('TEACHER'),
                             ('USER');

CREATE TABLE permissions (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL UNIQUE,
                             created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO permissions (name) VALUES
                                   ('user:write'),
                                   ('user:read'),
                                   ('course:write'),
                                   ('course:read');

CREATE TABLE role_permissions (
                                  id SERIAL PRIMARY KEY,
                                  role_id INTEGER NOT NULL,
                                  permission_id INTEGER NOT NULL,
                                  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
                                  CONSTRAINT fk_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,

                                  CONSTRAINT unique_role_permission UNIQUE(role_id, permission_id)
);
INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          (1, 1),
                                                          (1, 2),
                                                          (1, 3),
                                                          (1, 4),
                                                          (2, 3),
                                                          (2, 4),
                                                          (3, 4);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(250) NOT NULL,
                       email VARCHAR(250) NOT NULL UNIQUE,
                       phone VARCHAR(250) NOT NULL,
                       password VARCHAR(250) NOT NULL,
                       role_id INTEGER NOT NULL,
                       is_active BOOLEAN NOT NULL,
                       created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES roles(id)
);
CREATE INDEX idx_users_email ON users(email);
