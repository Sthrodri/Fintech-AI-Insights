#!/bin/bash
# Script para verificar que todos os ficheiros Docker foram criados
# Uso: bash ./verify-docker-setup.sh

echo "✓ Verificando configuração Docker..."
echo ""

MISSING=0

check_file() {
    if [ -f "$1" ]; then
        echo "✅ $1"
    else
        echo "❌ FALTA: $1"
        MISSING=$((MISSING + 1))
    fi
}

echo "Ficheiros Requeridos:"
check_file "Dockerfile"
check_file "docker-compose.yml"
check_file ".env.docker"
check_file "scripts/init-db.sql"

echo ""
echo "Ficheiros de Documentação:"
check_file "DOCKER_GUIDE.md"
check_file "DOCKER_SUMMARY.md"
check_file "README.md"

echo ""
echo "Scripts de Teste:"
check_file "test-docker.sh"
check_file "test-docker.ps1"

echo ""
echo "Ficheiros de Configuração:"
check_file "src/main/resources/application.yml"

if [ $MISSING -eq 0 ]; then
    echo "✅ Todos os ficheiros estão presentes!"
    echo ""
    echo "Próximo passo:"
    echo "  docker-compose up --build"
else
    echo "❌ Faltam $MISSING ficheiro(s)"
    exit 1
fi
