-- =====================================
-- TENANT 1 (Sthefany)
-- =====================================

INSERT INTO tenants (
    id,
    legal_name,
    trade_name,
    document,
    status,
    created_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440000',
    'Sthefany Fintech Alagoas Ltda',
    'Sthefany Fintech',
    '12.345.678/0001-90',
    'ACTIVE',
    '2024-01-01 00:00:00'
);

-- Usuário Tenant 1
INSERT INTO users (
    id,
    tenant_id,
    email,
    password_hash,
    created_at
) VALUES (
    '660e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440000',
    'admin@sthefany.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    '2024-01-01 00:00:00'
);

-- Transações Tenant 1
INSERT INTO transactions (
    id,
    description,
    amount,
    type,
    category,
    tenant_id,
    created_at
) VALUES (
    '770e8400-e29b-41d4-a716-446655440002',
    'Recebimento Projeto Java',
    5000.00,
    'ENTRADA',
    'Receitas',
    '550e8400-e29b-41d4-a716-446655440000',
    '2024-01-01 00:00:00'
);

INSERT INTO transactions (
    id,
    description,
    amount,
    type,
    category,
    tenant_id,
    created_at
) VALUES (
    '770e8400-e29b-41d4-a716-446655440003',
    'Pagamento Servidores AWS',
    800.00,
    'SAIDA',
    'Infraestrutura',
    '550e8400-e29b-41d4-a716-446655440000',
    '2024-01-01 00:00:00'
);

INSERT INTO transactions (
    id,
    description,
    amount,
    type,
    category,
    tenant_id,
    created_at
) VALUES (
    '770e8400-e29b-41d4-a716-446655440004',
    'Aluguel Escritório Jaraguá',
    1500.00,
    'SAIDA',
    'Aluguel',
    '550e8400-e29b-41d4-a716-446655440000',
    '2024-01-01 00:00:00'
);

INSERT INTO transactions (
    id,
    description,
    amount,
    type,
    category,
    tenant_id,
    created_at
) VALUES (
    '770e8400-e29b-41d4-a716-446655440005',
    'Consultoria People Analytics',
    3000.00,
    'ENTRADA',
    'Consultoria',
    '550e8400-e29b-41d4-a716-446655440000',
    '2024-01-01 00:00:00'
);

-- =====================================
-- TENANT 2 (Laísa) - sem transações
-- =====================================

INSERT INTO tenants (
    id,
    legal_name,
    trade_name,
    document,
    status,
    created_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Laisa Tech Ltda',
    'Laisa Tech',
    '98.765.432/0001-10',
    'ACTIVE',
    '2024-01-01 00:00:00'
);

-- Usuário Tenant 2
INSERT INTO users (
    id,
    tenant_id,
    email,
    password_hash,
    created_at
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'admin@laisa.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    '2024-01-01 00:00:00'
);