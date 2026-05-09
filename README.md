# API REST SaaS Multi-tenant para Gestão Financeira

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)
![Architecture](https://img.shields.io/badge/Architecture-Clean-brightgreen)

🔗 Repositório do projeto: [Fintech AI Insights](https://github.com/Sthrodri/Fintech-AI-Insights.git)

## 📋 Visão Geral

Sistema SaaS multi-tenant para gestão financeira com:
- ✅ Clean Architecture
- ✅ Multi-tenancy via Hibernate Filter + JWT
- ✅ Otimização de orçamento com Programação Dinâmica (Problema da Mochila)
- ✅ IA Generativa com Spring AI (RAG) + OpenAI ou Ollama
- ✅ Segurança com JWT e Spring Security
- ✅ Java 21 com Records e Virtual Threads
- ✅ Testes completos (JUnit 5 + Mockito)

---

## 🚀 Início Rápido

### ⭐ Opção Recomendada: Docker (Todos os serviços em 1 comando)

**Pré-requisitos:**
- Docker
- Docker Compose

**Execução:**

```bash
# Clone o repositório
git clone <repo-url>
cd api-financeira-saas

# Inicie tudo com um comando
docker-compose up --build
```

✅ Isso inicia:
- PostgreSQL 15 (banco de dados)
- Ollama (LLM local, gratuito)
- API Spring Boot (porta 8080)

Acesse: http://localhost:8080/swagger-ui.html

📖 **Para detalhes completos**, veja [DOCKER_GUIDE.md](./DOCKER_GUIDE.md) e [DOCKER_SUMMARY.md](./DOCKER_SUMMARY.md)

---

### 🖥️ Opção Alternativa: Execução Local (Requer Java 21 + Maven)

**Pré-requisitos:**
- Java 21+
- Maven 3.9+
- PostgreSQL 14+ (produção) ou H2 (desenvolvimento)

#### Passo 1: Clonar e Configurar

```bash
# Clonar o repositório
git clone <repo-url>
cd api-financeira-saas

# Copiar configuração de exemplo
cp .env.example .env
```

#### Passo 2: Configurar IA (Escolha Uma Opção)

**Opção A: OpenAI (Recomendado)**
```bash
# 1. Obtenha sua chave em https://platform.openai.com/api-keys

# 2. Configure a variável de ambiente
# Windows PowerShell:
$env:OPENAI_API_KEY = "sk-sua-chave-aqui"

# Linux/Mac:
export OPENAI_API_KEY="sk-sua-chave-aqui"

# 3. Inicie a aplicação
mvn spring-boot:run
```

**Opção B: Ollama (Gratuito, Local)**
```bash
# 1. Instale Ollama: https://ollama.ai

# 2. Baixe um modelo
ollama pull llama2

# 3. Inicie o Ollama em outro terminal
ollama serve

# 4. Descomente a configuração Ollama em src/main/resources/application.yml

# 5. Inicie a aplicação
mvn spring-boot:run
```

#### Passo 3: Verificar a Instalação

```bash
# Verificar endpoints
curl http://localhost:8080/swagger-ui.html

# Testar otimização de orçamento
curl -X POST http://localhost:8080/analysis/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "availableBudget": 1000.00,
    "items": [
      {"referenceId": "A1", "description": "Conta A", "amount": 100, "priorityScore": 8},
      {"referenceId": "B2", "description": "Conta B", "amount": 200, "priorityScore": 6}
    ]
  }'
```

---

## 📁 Estrutura do Projeto

```
src/main/java/com/projeto/
├── api/                      # Web Layer (Controllers, DTOs)
│   ├── controller/
│   ├── dto/
│   └── mapper/
├── application/              # Application Layer (Use Cases, Services)
│   ├── service/
│   ├── usecase/
│   └── port/
├── domain/                   # Domain Layer (Entities, Value Objects)
│   ├── entity/
│   ├── service/
│   └── valueobject/
└── infrastructure/           # Infrastructure Layer (Persistence, Security, AI)
    ├── repository/
    ├── persistence/
    ├── security/
    ├── ai/
    └── config/
```

---

## 🔐 Configurações de Segurança

### JWT
- Extrai `tenant_id` do token
- Armazena em `TenantContextHolder` (ThreadLocal)
- Filtra dados automaticamente por tenant no Hibernate

### Multi-tenancy
- `@FilterDef` e `@Filter` no Hibernate
- Garante isolamento de dados entre tenants
- Cada requisição é vinculada a um tenant via JWT

---

## 📊 Endpoints Principais

### Análise Financeira

#### 1. POST `/analysis/optimize`
Otimiza alocação de orçamento usando Programação Dinâmica

```bash
curl -X POST http://localhost:8080/analysis/optimize \
  -H "Authorization: Bearer <seu-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "availableBudget": 500.00,
    "items": [
      {"referenceId": "TX1", "description": "Conta de energia", "amount": 100.00, "priorityScore": 9},
      {"referenceId": "TX2", "description": "Internet", "amount": 80.00, "priorityScore": 8},
      {"referenceId": "TX3", "description": "Marketing", "amount": 200.00, "priorityScore": 5}
    ]
  }'
```

**Resposta:**
```json
{
  "selectedItems": [
    {"referenceId": "TX1", "description": "Conta de energia", "amount": 100.00, "priorityScore": 9},
    {"referenceId": "TX2", "description": "Internet", "amount": 80.00, "priorityScore": 8}
  ],
  "totalAmount": 180.00,
  "totalPriorityScore": 17.00
}
```

#### 2. GET `/analysis/insights`
Gera insights financeiros com RAG (Retrieval Augmented Generation)

```bash
curl -X GET http://localhost:8080/analysis/insights \
  -H "Authorization: Bearer <seu-jwt-token>"
```

**Resposta:**
```json
{
  "insight": "Com base no histórico recente, observe que: 1) Gastos operacionais representam 65% do orçamento... 2) Há oportunidade de reduzir custos em..."
}
```

---

## 🧪 Testes

### Executar Testes Unitários
```bash
# Testes da lógica de otimização
mvn test -Dtest=BudgetOptimizationServiceTest

# Testes do RAG
mvn test -Dtest=FinancialInsightsServiceTest

# Testes do controller
mvn test -Dtest=FinancialAnalysisControllerIT
```

### Executar Todos os Testes
```bash
mvn test
```

### Cobertura de Testes
```bash
mvn test jacoco:report
# Relatório: target/site/jacoco/index.html
```

---

## 📝 Documentação da API

A documentação interativa (Swagger/OpenAPI) está disponível em:

```
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

---

## 🔧 Configuração de Ambiente

### Desenvolvimento (H2 In-Memory)
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Produção (PostgreSQL)
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL=jdbc:postgresql://localhost:5432/financeira
export DB_USERNAME=finance_user
export DB_PASSWORD=senha-segura
export OPENAI_API_KEY=sk-sua-chave-aqui
mvn spring-boot:run
```

---

## 🆘 Troubleshooting

### Erro: "API Key not configured"
Ver: [OPENAI_SETUP.md](./OPENAI_SETUP.md)

### Erro: "Tenant context not available"
- Verifique se o header `Authorization` com JWT está sendo enviado
- Confirme que o token contém o claim `tenant_id`

### Erro: "Connection refused" (Ollama)
```bash
# Verifique se Ollama está rodando
ollama serve

# Teste a conexão
curl http://localhost:11434/api/tags
```

---

## 📚 Arquitetura

### Padrões Utilizados
- **Clean Architecture**: Separação clara entre camadas (Domain, Application, Infrastructure)
- **Strategy Pattern**: Cálculo de saldo dinâmico por status do tenant
- **Adapter Pattern**: Repositório ligando Application e Infrastructure
- **DTO Pattern**: Separação entre representação externa e interna

### Records (Java 21)
```java
public record BudgetOptimizationItem(
    String referenceId,
    String description,
    BigDecimal amount,
    BigDecimal priorityScore
) { }
```

### Hierarquias de Tipo
```java
public sealed interface TransactionRepository
    permits TransactionRepositoryAdapter { }
```

---

## 🚀 Deploy

### Docker
```bash
docker build -t api-financeira-saas .
docker run -e OPENAI_API_KEY=sk-xxx -p 8080:8080 api-financeira-saas
```

### Kubernetes
```bash
kubectl apply -f k8s/deployment.yaml
```

---

## 📞 Suporte e Contribuição

- Issues: [GitHub Issues](https://github.com/projeto/issues)
- Documentação de Setup da IA: [OPENAI_SETUP.md](./OPENAI_SETUP.md)
- Exemplo de Configuração: [.env.example](./.env.example)

---

## 📄 Licença

MIT License - veja [LICENSE](./LICENSE)
