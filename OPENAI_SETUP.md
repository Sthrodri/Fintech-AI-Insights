# Configuração da API OpenAI

## Problema: Chave da API OpenAI não configurada

Se você estiver recebendo erros sobre a chave da API OpenAI não estar configurada, siga as instruções abaixo.

## Solução 1: Usar OpenAI (Recomendado para Produção)

### 1. Obter a chave da API
1. Acesse [OpenAI Platform](https://platform.openai.com/)
2. Faça login em sua conta
3. Vá para **API keys** → **Create new secret key**
4. Copie a chave gerada

### 2. Configurar a variável de ambiente

#### Windows (PowerShell)
```powershell
$env:OPENAI_API_KEY = "sk-your-api-key-here"
```

#### Windows (CMD)
```cmd
set OPENAI_API_KEY=sk-your-api-key-here
```

#### Linux/macOS (Bash)
```bash
export OPENAI_API_KEY="sk-your-api-key-here"
```

#### Docker (no docker-compose.yml ou dockerfile)
```yaml
environment:
  OPENAI_API_KEY: sk-your-api-key-here
  OPENAI_MODEL: gpt-4o-mini  # ou outro modelo disponível
```

### 3. Verificar a configuração
Execute a aplicação e verifique se o endpoint `/analysis/insights` está funcionando.

---

## Solução 2: Usar Ollama (Local - Gratuito)

Se preferir não usar OpenAI ou não quer gastar com API, use Ollama localmente.

### 1. Instalar Ollama
- Download: [ollama.ai](https://ollama.ai)
- Windows/Mac: Instale normalmente
- Linux: 
```bash
curl https://ollama.ai/install.sh | sh
```

### 2. Baixar um modelo
```bash
ollama pull llama2  # ou outro modelo: mistral, neural-chat, etc
```

### 3. Iniciar o Ollama
```bash
ollama serve
```
(Roda na porta 11434 por padrão)

### 4. Ativar no projeto

Descomente no `src/main/resources/application.yml`:
```yaml
spring:
  ai:
    # Comentar (ou remover) a configuração OpenAI
    # openai:
    #   api-key: ...
    
    # Descomentar a configuração Ollama
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      model: ${OLLAMA_MODEL:llama2}
```

---

## Modelos Disponíveis

### OpenAI
- `gpt-4o-mini` (recomendado - rápido e barato)
- `gpt-4o`
- `gpt-3.5-turbo`

### Ollama
- `llama2` (7B - bom para começar)
- `mistral` (7B - mais rápido)
- `neural-chat` (7B - otimizado para chat)
- `orca-mini` (3B - leve)

---

## Diagnóstico de Problemas

### Erro: "api-key não está configurada"
- Verifique se a variável `OPENAI_API_KEY` está definida
- Reinicie a aplicação após definir a variável
- Use: `echo $OPENAI_API_KEY` (Linux/Mac) ou `echo %OPENAI_API_KEY%` (Windows)

### Erro: "Conexão recusada"
- Verifique se a API está acessível
- Teste: `curl https://api.openai.com/v1/models -H "Authorization: Bearer $OPENAI_API_KEY"`

### Erro ao usar Ollama: "Connection refused"
- Verifique se Ollama está rodando: `ollama serve`
- Teste a conexão: `curl http://localhost:11434/api/tags`

---

## Configuração para CI/CD

### GitHub Actions
```yaml
env:
  OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
  OPENAI_MODEL: gpt-4o-mini
```

### GitLab CI
```yaml
variables:
  OPENAI_API_KEY: $CI_JOB_TOKEN
  OPENAI_MODEL: gpt-4o-mini
```

---

## Estimativas de Custo (OpenAI)

Baseado em uso com `gpt-4o-mini`:
- 1M tokens de entrada: ~$0.15
- 1M tokens de saída: ~$0.60
- Média por insight: ~$0.01-0.05

Para desenvolvimento/teste: Use Ollama (gratuito localmente)
