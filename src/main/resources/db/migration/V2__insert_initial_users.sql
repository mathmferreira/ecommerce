-- Admin User
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    'admin@ecommerce.com',
    '$2a$10$aEymH.uv/m/VVBe0uZ5/senmFHABr0BPyHYv9Lv8Q2YZ07cTLm5mi',  -- admin
    'Administrator',
    'ADMIN',
    TRUE
);

-- Regular User
INSERT INTO user_tb (user_id, email, password, name, role, active)
VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    'user@ecommerce.com',
    '$2a$10$sT1eQ0kT3FPRCqFHUsWwKuVRXmRfP6hhCQ7hFT.WzhjNvLZz4cfsO',  -- user
    'Regular User',
    'USER',
    TRUE
);