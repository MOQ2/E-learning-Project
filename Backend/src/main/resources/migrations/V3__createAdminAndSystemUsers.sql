INSERT INTO users (
    name,
    email,
    phone,
    password,
    role_id,
    is_active,
    email_verified,
    profile_picture_url,
    bio
)
VALUES (
           'Admin',
           'admin@example.com',
           NULL,
           '$2a$10$8vVYjEQSE.oK.XTTt2C/NOUzTNMtHnnexP4MC2Gv0CSYPCJdgoVui',
           1,
           TRUE,
           TRUE,
           NULL,
           'System administrator with full access'
       )
    ON CONFLICT (email) DO NOTHING;

INSERT INTO users (
    name,
    email,
    phone,
    password,
    role_id,
    is_active,
    email_verified,
    profile_picture_url,
    bio
)
VALUES (
           'System',
           'system@example.com',
           NULL,
           '$2a$10$8vVYjEQSE.oK.XTTt2C/NOUzTNMtHnnexP4MC2Gv0CSYPCJdgoVui',
           3,
           TRUE,
           TRUE,
           NULL,
           'System user for automatic actions'
       )
    ON CONFLICT (email) DO NOTHING;
