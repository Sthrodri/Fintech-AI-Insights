#!/bin/bash
# Script para testar a aplicação em Docker
# Uso: bash ./test-docker.sh

set -e

echo "=========================================="
echo "Teste da Aplicação em Docker"
echo "=========================================="
echo ""

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para testar URL
test_url() {
    local url=$1
    local name=$2
    local max_attempts=30
    local attempt=1
    
    echo -n "Aguardando $name ($url)... "
    
    while [ $attempt -le $max_attempts ]; do
        if curl -sf "$url" > /dev/null 2>&1; then
            echo -e "${GREEN}✓ OK${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
        ((attempt++))
    done
    
    echo -e "${RED}✗ FALHOU${NC}"
    return 1
}

# Função para fazer requisição HTTP
test_endpoint() {
    local method=$1
    local url=$2
    local name=$3
    local body=$4
    
    echo ""
    echo "Testando: $name"
    echo "  Method: $method"
    echo "  URL: $url"
    
    if [ -z "$body" ]; then
        response=$(curl -s -X "$method" "$url" -H "Content-Type: application/json")
    else
        response=$(curl -s -X "$method" "$url" -H "Content-Type: application/json" -d "$body")
    fi
    
    echo "  Response: $response"
    echo ""
}

# Build e start dos containers
echo "Iniciando containers com docker-compose..."
docker-compose up -d --build

echo ""
echo "Aguardando inicialização dos serviços..."

# Testar PostgreSQL
if test_url "localhost:5432" "PostgreSQL"; then
    echo "  ✓ PostgreSQL está acessível"
else
    echo "  ✗ PostgreSQL não está acessível"
    exit 1
fi

# Testar Ollama
if test_url "http://localhost:11434" "Ollama"; then
    echo "  ✓ Ollama está acessível"
else
    echo "  ✗ Ollama não está acessível (pode estar baixando modelo)"
fi

# Testar API
if test_url "http://localhost:8080/swagger-ui.html" "API (Swagger UI)"; then
    echo "  ✓ API está funcionando"
else
    echo "  ✗ API não está respondendo"
    echo ""
    echo "Logs da aplicação:"
    docker-compose logs app
    exit 1
fi

echo ""
echo -e "${GREEN}=========================================="
echo "✓ Todos os serviços estão rodando!"
echo "==========================================${NC}"
echo ""
echo "Endpoints disponíveis:"
echo "  - Swagger UI:      http://localhost:8080/swagger-ui.html"
echo "  - OpenAPI JSON:    http://localhost:8080/v3/api-docs"
echo "  - PostgreSQL:      localhost:5432"
echo "  - Ollama:          http://localhost:11434"
echo ""
echo "Comandos úteis:"
echo "  docker-compose logs -f app       # Logs da aplicação (follow)"
echo "  docker-compose logs -f db        # Logs do PostgreSQL"
echo "  docker-compose logs -f ollama    # Logs do Ollama"
echo "  docker-compose down              # Parar todos os containers"
echo "  docker-compose down -v           # Parar e remover volumes"
echo ""
