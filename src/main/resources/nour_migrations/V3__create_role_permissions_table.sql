CREATE TABLE role_permissions (
                                  role_permission_id SERIAL PRIMARY KEY,
                                  role_id INTEGER NOT NULL,
                                  permission_id INTEGER NOT NULL,
                                  created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
                                  CONSTRAINT fk_permission FOREIGN KEY (permission_id) REFERENCES permissions(permission_id) ON DELETE CASCADE,

                                  CONSTRAINT unique_role_permission UNIQUE(role_id, permission_id)
);
