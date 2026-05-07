#!/bin/bash
# Script para testar a aplicação em Docker (versão Windows/PowerShell)
# Uso: .\test-docker.ps1

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Teste da Aplicação em Docker" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Função para testar URL
function Test-Url {
    param(
        [string]$Url,
        [string]$Name
    )
    
    Write-Host -NoNewline "Aguardando $Name ($Url)... "
    
    $maxAttempts = 30
    $attempt = 1
    
    while ($attempt -le $maxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2
            if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 404) {
                Write-Host "✓ OK" -ForegroundColor Green
                return $true
            }
        } catch {
            # Continue tentando
        }
        
        Write-Host -NoNewline "."
        Start-Sleep -Seconds 2
        $attempt++
    }
    
    Write-Host "✗ FALHOU" -ForegroundColor Red
    return $false
}

# Build e start dos containers
Write-Host "Iniciando containers com docker-compose..." -ForegroundColor Yellow
docker-compose up -d --build

Write-Host ""
Write-Host "Aguardando inicialização dos serviços..." -ForegroundColor Yellow

# Testar API
if (Test-Url "http://localhost:8080/swagger-ui.html" "API (Swagger UI)") {
    Write-Host "  ✓ API está funcionando"
} else {
    Write-Host "  ✗ API não está respondendo" -ForegroundColor Red
    Write-Host ""
    Write-Host "Logs da aplicação:" -ForegroundColor Red
    docker-compose logs app
    exit 1
}

# Testar Ollama
if (Test-Url "http://localhost:11434" "Ollama") {
    Write-Host "  ✓ Ollama está acessível"
} else {
    Write-Host "  ⚠ Ollama pode estar baixando modelo" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "✓ Aplicação está rodando com sucesso!" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Endpoints disponíveis:" -ForegroundColor Cyan
Write-Host "  - Swagger UI:      http://localhost:8080/swagger-ui.html"
Write-Host "  - OpenAPI JSON:    http://localhost:8080/v3/api-docs"
Write-Host "  - Ollama API:      http://localhost:11434"
Write-Host ""
Write-Host "Comandos úteis:" -ForegroundColor Cyan
Write-Host "  docker-compose logs -f app       # Logs da aplicação"
Write-Host "  docker-compose logs -f db        # Logs do PostgreSQL"
Write-Host "  docker-compose logs -f ollama    # Logs do Ollama"
Write-Host "  docker-compose down              # Parar containers"
Write-Host "  docker-compose down -v           # Parar e remover volumes"
Write-Host ""
