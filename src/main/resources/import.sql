-- Criando o Tenant (Sua Empresa SaaS)
INSERT INTO tenants (id, name, status) VALUES ('tenant-sthe-001', 'Sthefany Fintech Alagoas', 'ACTIVE');

-- Criando Transações de Exemplo para o RAG analisar
INSERT INTO transactions (description, amount, type, tenant_id) VALUES ('Recebimento Projeto Java', 5000.00, 'ENTRADA', 'tenant-sthe-001');
INSERT INTO transactions (description, amount, type, tenant_id) VALUES ('Pagamento Servidores AWS', 800.00, 'SAIDA', 'tenant-sthe-001');
INSERT INTO transactions (description, amount, type, tenant_id) VALUES ('Aluguel Escritório Jaraguá', 1500.00, 'SAIDA', 'tenant-sthe-001');
INSERT INTO transactions (description, amount, type, tenant_id) VALUES ('Consultoria People Analytics', 3000.00, 'ENTRADA', 'tenant-sthe-001');