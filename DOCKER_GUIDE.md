# Guia de Uso do Docker Compose para o Projeto

## Quick Start

Execute um único comando para iniciar toda a aplicação:

```bash
docker-compose up --build
```

Isso irá:
1. ✅ Compilar a aplicação Java com Maven
2. ✅ Iniciar o PostgreSQL 15
3. ✅ Iniciar o Ollama para LLM local
4. ✅ Iniciar a API em http://localhost:8080

## Acessar a Aplicação

Após iniciar, acesse:

- **Swagger UI (Documentação API)**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **PostgreSQL**: `localhost:5432` (user: `finance_user`, password: `finance_pass_dev`)
- **Ollama**: http://localhost:11434

## Configurações de Ambiente

### Variáveis Disponíveis

Você pode customizar o comportamento criando um arquivo `.env` na raiz do projeto:

```bash
# Copiar o template
cp .env.docker .env

# Editar com suas variáveis
# Exemplo: trocar senha do PostgreSQL, usar OpenAI em vez de Ollama, etc.
```

### Usar OpenAI em vez de Ollama

1. Edite o `docker-compose.yml` ou crie um `.env` com:

```bash
OPENAI_API_KEY=sk-sua-chave-aqui
```

2. Altere o `application.yml` para comentar Ollama e descomentarOpenAI

### Usar um Banco de Dados PostgreSQL Remoto

```bash
DB_URL=jdbc:postgresql://seu-host:5432/financeira
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha
```

## Comandos Úteis

### Iniciar Serviços
```bash
# Build e inicia (mostra logs no console)
docker-compose up --build

# Build e inicia em background
docker-compose up -d --build

# Apenas inicia (sem rebuild)
docker-compose up
```

### Visualizar Logs
```bash
# Todos os logs
docker-compose logs

# Apenas aplicação (follow)
docker-compose logs -f app

# Apenas banco de dados
docker-compose logs -f db

# Apenas Ollama
docker-compose logs -f ollama
```

### Parar Serviços
```bash
# Parar (mantém volumes)
docker-compose down

# Parar e remover volumes (limpa dados)
docker-compose down -v

# Parar e remover tudo (incluindo imagens)
docker-compose down -v --rmi all
```

### Acessar Containers
```bash
# Shell do container da aplicação
docker-compose exec app sh

# Shell do PostgreSQL
docker-compose exec db bash

# Shell do Ollama
docker-compose exec ollama bash

# Conectar ao PostgreSQL via psql
docker-compose exec db psql -U finance_user -d financeira
```

### Reiniciar Serviços
```bash
# Reiniciar todos
docker-compose restart

# Reiniciar um serviço específico
docker-compose restart app
```

### Gerenciar Modelos Ollama
```bash
# Dentro do container Ollama, listar modelos
docker-compose exec ollama ollama list

# Baixar um modelo (dentro do container)
docker-compose exec ollama ollama pull llama2
docker-compose exec ollama ollama pull mistral
```

## Teste Rápido da API

### Otimizar Orçamento

```bash
curl -X POST http://localhost:8080/analysis/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "availableBudget": 100,
    "items": [
      {"referenceId": "1", "description": "Item 1", "amount": 50, "priorityScore": 8},
      {"referenceId": "2", "description": "Item 2", "amount": 60, "priorityScore": 7}
    ]
  }'
```

### Gerar Insights Financeiros

```bash
# Nota: Requer token JWT válido (veja endpoint de autenticação)
curl -X GET http://localhost:8080/analysis/insights \
  -H "Authorization: Bearer SEU_TOKEN_JWT"
```

## Troubleshooting

### "Port already in use"

Se a porta 8080 ou 5432 está ocupada:

```bash
# Alterar porta no docker-compose.yml
# Exemplo: mudar porta da API de 8080:8080 para 8090:8080
```

### PostgreSQL não inicializa

```bash
# Verificar logs
docker-compose logs db

# Remover volume e recriá-lo
docker-compose down -v
docker-compose up -d db
```

### Ollama não baixa modelo automaticamente

Os modelos não são baixados automaticamente (para economizar espaço).

Para usar Ollama, você precisa baixar um modelo manualmente:

```bash
# Depois que o Ollama estiver rodando
docker-compose exec ollama ollama pull llama2

# Ou via API
curl http://localhost:11434/api/pull -d '{"name":"llama2"}'
```

### Aplicação não conecta ao banco de dados

1. Verifique que o container `db` está rodando:
   ```bash
   docker-compose ps
   ```

2. Verifique os logs:
   ```bash
   docker-compose logs app
   ```

3. Verificar credenciais no docker-compose.yml

### API não responde

```bash
# Verificar logs detalhados
docker-compose logs -f app

# Verificar saúde do container
docker ps

# Reiniciar aplicação
docker-compose restart app
```

## Performance e Otimização

### Limpar espaço em disco

```bash
# Remove containers inutilizados
docker container prune

# Remove imagens inutilizadas
docker image prune -a

# Remove volumes inutilizados
docker volume prune
```

### Aumentar recursos (se necessário)

Edite o `docker-compose.yml`:

```yaml
app:
  deploy:
    resources:
      limits:
        cpus: '2'
        memory: 1G
      reservations:
        cpus: '1'
        memory: 512M
```

## Arquivos Importantes

- **Dockerfile**: Define como compilar e empacotar a aplicação
- **docker-compose.yml**: Orquestra os 3 serviços (app, db, ollama)
- **.env.docker**: Template com variáveis de ambiente
- **application.yml**: Configuração Spring Boot com suporte a variáveis de ambiente
- **scripts/init-db.sql**: Script de inicialização do PostgreSQL
- **scripts/init-ollama.sh**: Script de inicialização do Ollama (opcional)

## Próximos Passos

1. ✅ Execute `docker-compose up --build`
2. ✅ Acesse http://localhost:8080/swagger-ui.html
3. ✅ Teste os endpoints na interface Swagger
4. ✅ Verifique os logs com `docker-compose logs -f`
5. ✅ Customize configurações conforme necessário

Boa sorte! 🚀
