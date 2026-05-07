#!/bin/bash

# Script de inicialização do Ollama
# Descomente e customize conforme necessário

set -e

echo "Esperando Ollama ficar pronto..."
sleep 10

# Listar modelos disponíveis
echo "Modelos disponíveis no Ollama:"
curl -s http://localhost:11434/api/tags | jq '.models[].name' 2>/dev/null || echo "Nenhum modelo ainda"

# Puxar modelo padrão (llama2)
# Descomente a linha abaixo para fazer download automático (pode levar alguns minutos)
# echo "Puxando modelo llama2..."
# curl -X POST http://localhost:11434/api/pull -d '{"name":"llama2"}'

echo "Ollama initialization completed"
