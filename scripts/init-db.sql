#!/bin/bash

# Script de inicialização do banco de dados PostgreSQL
# Executado automaticamente quando o container db inicia

set -e

# Criar extensões necessárias
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Criar extensão UUID se não existir
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

    -- Criar schema para a aplicação
    CREATE SCHEMA IF NOT EXISTS financeira_schema;

    -- Conceder permissões ao usuário
    GRANT ALL PRIVILEGES ON SCHEMA financeira_schema TO $POSTGRES_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA financeira_schema GRANT ALL ON TABLES TO $POSTGRES_USER;
    ALTER DEFAULT PRIVILEGES IN SCHEMA financeira_schema GRANT ALL ON SEQUENCES TO $POSTGRES_USER;

    -- Log de sucesso
    SELECT 'Database initialized successfully!' as message;
EOSQL

echo "PostgreSQL initialization completed successfully"
