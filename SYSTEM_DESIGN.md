# 📊 System Design Documentation

## 1. Visão Geral

API REST SaaS desenvolvida em **Java 21** com **Spring Boot 3.4**, voltada para empresas que precisam gerenciar perfis financeiros de seus usuários com isolamento total de dados entre clientes (tenants).

### Padrões Arquiteturais
- **Clean Architecture**: Separação em 4 camadas (API, Application, Domain, Infrastructure)
- **Multi-tenancy**: Isolamento `tenant_id` via Hibernate Filters + JWT
- **Token Bucket**: Rate limiting com recarga gradual por tenant
- **Adapter Pattern**: Repositórios ligando Application e Infrastructure
- **Strategy Pattern**: Cálculo dinâmico de saldo por status do tenant
- **Hexagonal Architecture**: Ports & Adapters isolam o domínio de infraestrutura

---

## 🏛️ Arquitetura em Camadas

```
┌─────────────────────────────────────────────────────────────────┐
│              API LAYER (REST Controllers)                       │
│     Controllers, DTOs, OpenAPI/Swagger, Validators              │
├─────────────────────────────────────────────────────────────────┤
│           APPLICATION LAYER (Business Logic)                    │
│  Use Cases: BudgetOptimization, FinancialInsights               │
│  Services: Login, TenantBalance, FinancialInsights              │
│  Ports (interfaces): TransactionRepository, FinancialInsightPort│
├─────────────────────────────────────────────────────────────────┤
│              DOMAIN LAYER (Pure Business Rules)                 │
│  Entities: Transaction, Tenant, User — implementadas como       │
│  Java Records (Java 21) para imutabilidade                      │
│  Value Objects: TenantId, TransactionId, UserId                 │
│  Domain Services: BalanceCalculationStrategy                    │
├─────────────────────────────────────────────────────────────────┤
│          INFRASTRUCTURE LAYER (Technical Details)               │
│  Persistence: Spring Data JPA + Hibernate                       │
│  Security: JWT + Spring Security + Bucket4j                    │
│  AI Integration: Spring AI (OpenAI/Ollama)                      │
│  Database: PostgreSQL (prod) / H2 (dev)                         │
└─────────────────────────────────────────────────────────────────┘
```

### Fluxo de Requisição Autenticada

```
Cliente (HTTP)
     │
     ▼
[1] RateLimitFilter → Valida 100 req/min por tenant (Bucket4j)
     │
     ▼
[2] JwtAuthenticationFilter → Extrai tenant_id do JWT
     │
     ▼
[3] TenantContextHolder → Armazena tenant_id em ThreadLocal
     │
     ▼
[4] TenantHibernateFilter → Aplica WHERE tenant_id = ? automaticamente
     │
     ▼
[5] Controller (Endpoint)
     │
     ▼
[6] Use Case / Service → Executa regra de negócio
     │
     ▼
[7] Repository (JPA) → Persiste/busca dados do tenant
     │
     ▼
Database (H2 ou PostgreSQL) → Retorna apenas dados isolados
```

---

## 2. Segurança

### Autenticação JWT
- **Endpoint**: `POST /auth/login` (público)
- **Entrada**: email + senha
- **Saída**: JWT assinado com HMAC-SHA384 contendo `tenant_id` embutido
- **Validade**: 1 hora
- **Armazenamento de Senha**: BCrypt (nunca em texto puro)

### Spring Security
- **Sessão**: Stateless (sem cookies, sem estado no servidor)
- **Rotas Públicas**: `/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`
- **Rotas Protegidas**: `/analysis/**` — exigem JWT válido no header `Authorization: Bearer <token>`
- **Filtro de Tenant**: Cada requisição vinculada ao tenant_id do JWT automaticamente

---

## 3. Multi-tenancy — Isolamento de Dados

O isolamento é implementado em três níveis simultâneos:

### Nível 1: JWT (Autenticação)
- O token JWT contém `tenant_id` embutido
- `JwtAuthenticationFilter` extrai e valida o token a cada requisição

### Nível 2: ThreadLocal (Contexto)
- `TenantContextHolder` armazena `tenant_id` na thread atual
- ThreadLocal garante que a thread não processe dados de outro tenant

### Nível 3: Banco de Dados (Filtro SQL)
- Hibernate `@FilterDef` + `@Filter` aplicam `WHERE tenant_id = ?` automaticamente
- Nenhuma consulta esquece de filtrar por tenant — proteção em nível SQL

**Resultado**: Empresa A nunca vê dados da Empresa B, mesmo que consiga contornar validações de aplicação.

---

## 4. Rate Limiting — Controle de Concorrência

Implementado com **Bucket4j** usando algoritmo **Token Bucket**:

```
Configuração: 100 requisições por minuto por tenant

Tenant A: [████████████] 100 tokens/min  → ✅ Requisições OK
Tenant B: [░░░░░░░░░░░░]   0 tokens/min  → ❌ HTTP 429 Too Many Requests
```

### Características
- **ConcurrentHashMap**: Armazena um balde por tenant (thread-safe)
- **Recarga Gradual**: 100 tokens recarregam continuamente a cada minuto
- **Isolamento**: Consumo de um tenant não afeta outro
- **Isenções**: Rotas públicas (`/auth/`) não são limitadas
- **Resposta HTTP 429**: JSON com identificador do tenant

### Implementação
```java
// RateLimitFilter.java
private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

Bucket bucket = buckets.computeIfAbsent(tenantId, key -> createNewBucket());
if (bucket.tryConsume(1)) {
    filterChain.doFilter(request, response);  // ✅ Permite
} else {
    response.setStatus(429);                   // ❌ Rate limit excedido
    response.getWriter().write(String.format(
        "{\"error\": \"Too Many Requests\", \"tenant\": \"%s\"}", 
        tenantId
    ));
}
```

---

## 5. Estratégias de Cache

### Cache de Aplicação (In-Memory)
- **Rate Limiting**: `ConcurrentHashMap<String, Bucket>` por tenant
  - Performance: Evita consultar estado externo do bucket
  - Isolamento: Cada tenant tem seu próprio balde

- **Tenant Context**: `ThreadLocal<String>` para isolamento por requisição
  - Performance: Acesso O(1) ao tenant_id da thread atual
  - Isolamento: Cada thread processa apenas seu tenant

- **Benefício Geral**: Performance de acesso + Isolamento de dados garantido

### Cache de Banco de Dados
- **Hikari Connection Pool**: Pool de conexões configurado
  - Reutiliza conexões entre requisições
  - Configuração: max 10 conexões em produção
  - Timeout: 20 segundos para obter conexão

- **Prepared Statements**: Reutilização automática pelo JDBC
  - Evita re-parsing de SQL
  - Prevenção de SQL injection

- **Query Result Cache**: Não implementado atualmente
  - Candidato: Resultado de `/analysis/optimize` (orçamento + itens)
  - Candidato: Insights gerados pela IA (TTL 5 min por tenant)
  - Implementação recomendada: **Spring Cache + Redis**

### Estratégia Redis Futura
```yaml
# application-prod.yml (proposto)
spring:
  cache:
    type: redis
  redis:
    host: redis
    port: 6379
    timeout: 2000ms

cache:
  caffeine:
    spec: maximumSize=500,expireAfterWrite=10m
  ttl:
    insights: 5m
    optimization: 30m
```

---

---

## 6. Resiliência e Circuit Breakers

### Configurações Atuais
- **Connection Pool (Hikari)**: Timeout de 20 segundos + retry automático
  - Máximo 10 conexões em produção
  - Detecta e remove conexões mortas
  - Previne resource exhaustion

- **Database Failover**: PostgreSQL em Docker Compose com health checks
  - Restart automático em caso de falha
  - Dados persistidos em volume

- **AI Service Fallback**: 
  - Tenta OpenAI primeiro (melhor qualidade)
  - Se falhar, cai para Ollama local (sempre disponível)
  - Se ambos falharem, retorna mensagem informativa

### Circuit Breaker Pattern (Proposto — Resilience4j)

```java
@Service
public class ChatService implements FinancialInsightPort {
    
    @CircuitBreaker(
        name = "aiService", 
        fallbackMethod = "fallbackResponse"
    )
    @Override
    public String generateInsight(String context) {
        String tenantId = tenantContextHolder.getTenantId();
        // Tenta OpenAI
        return openAiChatModel.call(createPrompt(context));
    }
    
    // Fallback quando circuit breaker abre
    public String fallbackResponse(String context, Exception e) {
        log.warn("OpenAI indisponível, usando Ollama");
        return ollamaChatModel.call(createPrompt(context));
    }
}
```

### Retry Policies (Proposto)

```yaml
# application-prod.yml
resilience4j:
  retry:
    instances:
      aiService:
        maxAttempts: 3
        waitDuration: 1000
        enableExponentialBackoff: true
        retryExceptions:
          - java.net.SocketTimeoutException
          - org.springframework.web.client.ResourceAccessException
  
  circuitbreaker:
    instances:
      aiService:
        failureRateThreshold: 50       # Abre se 50% das chamadas falham
        waitDurationInOpenState: 10000  # Aguarda 10s antes de testar
        slowCallRateThreshold: 70
        slowCallDurationThreshold: 2000 # Chamadas > 2s são lentas
        ringBufferSizeInClosedState: 100
```

### Benefícios
- **Proteção contra cascata de falhas**: Circuit breaker impede chamadas desnecessárias
- **Recuperação automática**: Exponential backoff distribui retry
- **Fallback gracioso**: Ollama local sempre disponível como plano B
- **Monitoramento**: Métricas de circuit state para alertas

---

## 7. Escalabilidade

### Escalabilidade Horizontal
- **Stateless Design**: JWT elimina estado do servidor → múltiplas instâncias independentes
- **Load Balancer**: Nginx/HAProxy distribui requisições
- **Database Sharding**: Possível por `tenant_id` após atingir limites de throughput
- **Message Queue**: Redis/RabbitMQ para processamento assíncrono (futuro)

### Escalabilidade Vertical
- **JVM Tuning**: Java 21 com **Virtual Threads** habilitam alta concorrência com baixo consumo
- **Connection Pool (Hikari)**: Máximo 10 conexões em produção, suficiente para alta concorrência com threads virtuais
- **Garbage Collection**: G1GC otimizado para aplicações web

### Métricas de Scalabilidade
| Métrica | Valor | Limitado Por |
|---------|-------|--------------|
| Throughput | 100 req/min por tenant | Rate limiting |
| Latency | <200ms endpoints principais | Network + DB |
| Concurrent Tenants | Ilimitado | Limite de memória |
| Concurrent Users | Linear com JVM memory | Virtual Threads |
| Connections DB | 10 (prod) | Hikari pool-size |

---

## 8. Banco de Dados

### Ambientes
| Ambiente | Banco | Driver | Estratégia DDL |
|----------|-------|--------|-----------------|
| **Desenvolvimento** | H2 in-memory | H2 | `update` (auto-cria tabelas) |
| **Produção** | PostgreSQL 15 | `org.postgresql.Driver` | `validate` (migrations obrigatórias) |

### Modelo de Dados

```
┌─────────────────────────────────────────────────────┐
│ tenants (Empresas)                                  │
├─────────────────────────────────────────────────────┤
│ id           → UUID PK                              │
│ legal_name   → Nome oficialmente registrado         │
│ trade_name   → Nome comercial                       │
│ status       → ACTIVE | INACTIVE                    │
└─────────────────────────────────────────────────────┘
                       │
          ┌────────────┼────────────┐
          │                         │
          ▼                         ▼
    ┌──────────────┐          ┌────────────────┐
    │ users        │          │ transactions   │
    ├──────────────┤          ├────────────────┤
    │ id (UUID)    │          │ id (UUID)      │
    │ tenant_id ←──┼──────┐   │ tenant_id ←────┤
    │ email        │      │   │ description    │
    │ password_hash│      │   │ amount (DECIMAL)
    └──────────────┘      │   │ type (ENUM)    │
                          │   │ category       │
                          │   │ created_at     │
                          │   └────────────────┘
                          │
                    Chave estrangeira
                    Garante isolamento
```

### Configuração Hikari Connection Pool (Produção)
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10      # Máx conexões
      minimum-idle: 5             # Mínimo idle
      connection-timeout: 20000   # 20s para obter conexão
      idle-timeout: 300000        # Fecha após 5min idle
      max-lifetime: 1200000       # Max 20min por conexão
      leak-detection-threshold: 60000
```

### PostgreSQL Tuning (Proposto)
```sql
-- Arquivo: docker-entrypoint-initdb.d/init.sql
ALTER SYSTEM SET max_connections = '200';
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET work_mem = '4MB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
```

### Indexes Recomendados
```sql
-- Performance nas operações de filtro por tenant
CREATE INDEX idx_transactions_tenant_id 
  ON transactions(tenant_id);

CREATE INDEX idx_transactions_tenant_created 
  ON transactions(tenant_id, created_at DESC);

CREATE INDEX idx_users_tenant_email 
  ON users(tenant_id, email);
```

### Isolation Level
```yaml
spring:
  jpa:
    properties:
      hibernate:
        connection.isolation: 2  # READ_COMMITTED (padrão recomendado)
```

---

## 9. Inteligência Artificial com RAG (Retrieval Augmented Generation)

### Endpoint: GET `/analysis/insights`

**Fluxo**:
1. **Retrieve**: Busca até 20 transações recentes do tenant autenticado
2. **Augment**: Monta contexto estruturado com os dados reais
3. **Generate**: Envia par modelo LLM rodando localmente via Ollama
4. **Return**: Resposta em linguagem natural

**Implementação**:
```java
@GetMapping("/insights")
public ResponseEntity<FinancialInsightResponse> insights() {
    // 1. Valida tenant_id do JWT via TenantContextHolder
    // 2. Busca transações filtradas por tenant_id
    List<Transaction> recentTransactions = 
        transactionRepository.findRecent(20);
    
    // 3. Estrutura o contexto
    String context = recentTransactions.stream()
        .map(t -> String.join(" | ",
            t.createdAt().toString(),
            t.description(),
            t.category(),
            t.type().name(),
            t.amount().toPlainString()))
        .collect(Collectors.joining("\n"));
    
    // 4. Chama IA com contexto do tenant
    String insight = chatService.generateInsight(context);
    
    return ResponseEntity.ok(
        new FinancialInsightResponse(insight)
    );
}
```

**Garantias de Isolamento**:
- `TenantContextHolder` garante que apenas dados do tenant são recuperados
- `@Filter(condition = "tenant_id = :tenantId")` no Hibernate filtra SQL
- Prompt system não expõe dados sensíveis — apenas análise agregada

---

## 10. Algoritmo de Otimização: Problema da Mochila

### Endpoint: POST `/analysis/optimize`

**Problema**: Dado orçamento B e lista de itens com valor monetário e prioridade, selecionar combinação que maximize prioridade total sem exceder B.

**Algoritmo**: Programação Dinâmica (Dynamic Programming)
- **Complexidade**: O(n × W) onde n = número de itens, W = orçamento
- **Espaço**: O(W) com otimização de espaço

**Pseudocódigo**:
```
dp[0..W] = [0] * (W+1)

for cada item i with (custo, prioridade):
    for j from W descendo até custo:
        dp[j] = max(dp[j], dp[j-custo] + prioridade)

return max(dp)
```

**Exemplo**:
```
Orçamento: 500

Item       | Valor | Prioridade
-----------|-------|----------
Energia    | 100   | 9
Internet   | 80    | 8
Marketing  | 200   | 5
Aluguel    | 150   | 10

Resultado ótimo: Energia (100) + Internet (80) + Aluguel (150) = 330 gasto
Prioridade total: 9 + 8 + 10 = 27
```

---

## 11. Endpoints da API

| Método | Endpoint | Auth | Descrição | Limitado |
|--------|----------|------|-----------|----------|
| POST | `/auth/login` | ❌ | Login e geração de JWT | ❌ |
| POST | `/analysis/optimize` | ✅ JWT | Otimização de orçamento (DP) | ✅ 100/min |
| GET | `/analysis/insights` | ✅ JWT | Insights financeiros (RAG) | ✅ 100/min |
| GET | `/swagger-ui.html` | ❌ | Documentação interativa | ❌ |
| GET | `/v3/api-docs` | ❌ | OpenAPI YAML/JSON | ❌ |

### Exemplo de Request Autenticado

```bash
curl -X GET http://localhost:8080/analysis/insights \
  -H "Authorization: Bearer eyJhbGc.eyJsub.SflKxwR..."
```

**Headers gerados internamente**:
1. `JwtAuthenticationFilter` → extrai tenant_id do JWT → armazena em TenantContextHolder
2. `RateLimitFilter` → valida rate limit do tenant → passa ou retorna 429
3. `TenantHibernateFilter` → aplica filter SQL automaticamente

---

## 12. Monitoramento e Observabilidade

### Health Checks
```yaml
# Proposto para application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: always
  health:
    db:
      enabled: true
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

**Checks implementados**:
- ✅ **Database**: Conectividade PostgreSQL/H2
- ✅ **JVM**: Heap usage, thread count
- ✅ **Disk**: Espaço disponível
- 🔄 **AI Services**: Disponibilidade Ollama/OpenAI (proposto)
- 🔄 **Tenant Integrity**: Validação de dados isolados (proposto)

### Logging Estruturado
```yaml
logging:
  level:
    root: INFO
    com.projeto: ${LOGGING_LEVEL_COM_PROJETO:INFO}
    org.springframework.security: ${LOGGING_LEVEL_SPRING_SECURITY:WARN}
    org.hibernate.SQL: ${LOGGING_LEVEL_HIBERNATE:WARN}
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - tenant=%X{tenantId} - %msg%n"
```

### Métricas Expostas (Prometheus)
```
# Exemplo
api_requests_total{endpoint="/analysis/optimize",tenant_id="company-a"} 1250
api_requests_duration_seconds{endpoint="/analysis/insights"} 0.245
rate_limit_exceeded_total{tenant_id="company-b"} 5
db_connection_pool_active{} 7
```

---

## 13. Estratégias de Deploy

### Desenvolvimento Local
```bash
# Com H2 in-memory
mvn spring-boot:run -Dspring-profiles.active=dev

# Acesso: http://localhost:8081/swagger-ui.html
```

### Produção — Docker Compose
```yaml
version: '3.8'
services:
  app:
    image: api-financeira-saas:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/financeira
      - SECURITY_JWT_SECRET=${JWT_SECRET}
      - OPENAI_API_KEY=${OPENAI_API_KEY:-}
    depends_on:
      db:
        condition: service_healthy
      ollama:
        condition: service_started

  db:
    image: postgres:15
    environment:
      POSTGRES_DB: financeira
      POSTGRES_USER: finance_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U finance_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  ollama:
    image: ollama/ollama:latest
    volumes:
      - ollama:/root/.ollama
    command: serve

volumes:
  pgdata:
  ollama:
```

### Kubernetes (Futuro)
```yaml
# HorizontalPodAutoscaler sugerido
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: api-financeira-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: api-financeira-saas
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

---

## 14. Otimizações de Performance

### JVM Configuration (Produção)
```bash
# JAVA_OPTS para produção
export JAVA_OPTS="
  -Xms512m 
  -Xmx1024m 
  -XX:+UseG1GC 
  -XX:MaxGCPauseMillis=200 
  -XX:+UseStringDeduplication 
  -XX:+ParallelRefProcEnabled
  --enable-preview
"
```

### Database Optimization
- **Indexes**: `tenant_id`, `tenant_id + created_at` implementados
- **Query Plans**: Analisar via `EXPLAIN ANALYZE`
- **Connection Pooling**: Hikari com 10 conexões
- **Batch Operations**: Para processing em lote

### AI Service Optimization
- **Request Caching**: Resultados similares (TTL 5min)
- **Async Processing**: CompletableFuture para insights pesados
- **Fallback Chain**: OpenAI → Ollama → Cached Response

---

## 15. Tecnologias Utilizadas

| Componente | Tecnologia | Versão | Razão |
|------------|-----------|--------|-------|
| **Linguagem** | Java | 21 | Records, Virtual Threads |
| **Framework** | Spring Boot | 3.4.5 | Produtivo, maduro |
| **Segurança** | Spring Security | 6.4 | Standard de mercado |
| **JWT** | JJWT | 0.12.6 | Leve, confiável |
| **IA** | Spring AI | 1.0.0-M6 | Abstração de LLMs |
| **ORM** | Hibernate | 6.6 | Filters multi-tenant |
| **Rate Limiting** | Bucket4j | 8.10.1 | Eficiente, sem DB |
| **API Docs** | Springdoc | 2.8.9 | OpenAPI 3.1 |
| **Banco (Dev)** | H2 | - | In-memory, rápido |
| **Banco (Prod)** | PostgreSQL | 15 | Confiável, open-source |
| **LLM (Local)** | Ollama | latest | Gratuito, local |
| **LLM (Cloud)** | OpenAI | GPT-4 / GPT-3.5 | SOTA, confiável |

---

## 🎯 Conclusão e Próximos Passos

### O que foi implementado ✅
- Clean Architecture rigorosa com 4 camadas bem separadas  
- Multi-tenancy com isolamento em 3 níveis (JWT + ThreadLocal + SQL)  
- Rate limiting por tenant (100 req/min) com Bucket4j  
- Autenticação JWT com tenant_id embutido  
- RAG (Retrieval Augmented Generation) com Spring AI  
- Programação Dinâmica para otimização de orçamento  
- Java 21 com Records e sealed interfaces

---
## Desenvolvido por

- [Sthefany Silva](https://linkedin.com/in/sthefany-rodrigues-silva)
- [Laísa Ferreira da Silva](https://www.linkedin.com/in/la%C3%ADsa-ferreira-da-silva-)


---
*Documento atualizado: 08/05/2026 — Hackathon Artemísia Elas Tech — Trilha Backend*