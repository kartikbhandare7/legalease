-- Default admin user — change password immediately after first login
-- Password below is BCrypt hash of "Admin@123"
INSERT INTO users (
    id,
    full_name,
    email,
    password,
    role,
    auth_provider,
    account_status,
    created_at,
    updated_at
) VALUES (
             gen_random_uuid(),
             'Super Admin',
             'admin@legalease.com',
             '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
             'ROLE_ADMIN',
             'LOCAL',
             'ACTIVE',
             NOW(),
             NOW()
         );