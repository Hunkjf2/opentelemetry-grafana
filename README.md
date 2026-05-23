# 🏗️ Arquitetura de Microsserviços com Spring Boot, RabbitMQ e Redis

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5+-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-orange.svg)](https://www.rabbitmq.com/)
[![Redis](https://img.shields.io/badge/Redis-7.2-red.svg)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org/)
[![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-OTLP-blueviolet.svg)](https://opentelemetry.io/)
[![Grafana](https://img.shields.io/badge/Grafana-LGTM-orange.svg)](https://grafana.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Visão Geral

Sistema distribuído implementando arquitetura de microsserviços com **Spring Boot 3**, **RabbitMQ** para comunicação entre serviços e **Redis** para cache distribuído. O projeto demonstra na prática os principais padrões de arquitetura de microsserviços: mensageria síncrona e assíncrona, circuit breaker, cache-aside, write-through, tratamento de erros centralizado, métricas customizadas e observabilidade completa com OpenTelemetry + Grafana LGTM.

---

## 🏛️ Diagrama de Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                           Cliente HTTP                              │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼ POST/PUT/DELETE /api/pessoa
┌─────────────────────────────────────────────────────────────────────┐
│                    microsservico-pessoa  :8090                      │
│                                                                     │
│   PessoaController → PessoaService                                  │
│                           │                                         │
│              ┌────────────┴────────────┐                           │
│              │                         │                           │
│    SerasaService               LogService                           │
│    (Circuit Breaker)           (fire-and-forget)                    │
│              │                         │                           │
└──────────────┼─────────────────────────┼───────────────────────────┘
               │                         │
               │ verificar-serasa-request │ enviar-log
               │ (síncrono, timeout 3s)  │ (assíncrono)
               ▼                         ▼
┌──────────────────────────┐   ┌─────────────────────────────────────┐
│  microsservico-serasa    │   │      microsservico-log  :8060        │
│  :8070                   │   │                                     │
│                          │   │  LogConsumerService                 │
│  SerasaConsumerService   │   │       │                             │
│  SerasaService           │   │  LogService                         │
│  (mock de CPFs)          │   │       ├── PostgreSQL (logs_db)      │
│          │               │   │       └── Redis (Cache-Aside)       │
│          │ verificar-    │   │                                     │
│          │ serasa-       │   │  GET /api/logs                      │
│          │ response      │   │  LogCacheService                    │
└──────────┼───────────────┘   │  (Redis → fallback PostgreSQL)      │
           │                   └─────────────────────────────────────┘
           └──────────────────► microsservico-pessoa (Boolean negativado)

┌─────────────────────────────────────────────────────────────────────┐
│                    Infraestrutura                                   │
│                                                                     │
│  RabbitMQ :5672/:15672   Redis :6379/:5540   PostgreSQL :5432      │
│                                                                     │
│  Grafana LGTM :3000/:4318                                          │
│  (Mimir + Tempo + Loki + OTel Collector)                           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📦 Estrutura do Repositório

```
microsservicos-spring-rabbitmq/
├── microsservico-pessoa/          # Gerenciamento de pessoas (porta 8090)
├── microsservico-serasa/          # Simulação de consulta Serasa (porta 8070)
├── microsservico-log/             # Auditoria com Redis cache (porta 8060)
├── rabbitmq/
│   ├── docker-compose.yml
│   └── rabbitmq.conf
├── redis/
│   └── docker-compose.yml         # Redis + RedisInsight
├── grafana/
│   └── docker-compose.yml         # Grafana LGTM (all-in-one)
└── README.md                      # Este arquivo
```

---

## 🎯 Padrões Arquiteturais Implementados

| Padrão | Onde | Descrição |
|--------|------|-----------|
| **Request-Reply** | Pessoa → Serasa | Comunicação síncrona com timeout de 3s |
| **Fire-and-Forget** | Pessoa → Log | Envio assíncrono de eventos de auditoria |
| **Circuit Breaker** | SerasaService | Resilience4j protege a chamada ao Serasa |
| **Cache-Aside** | LogCacheService | Lê Redis → miss → busca DB → popula Redis |
| **Write-Through** | LogService | Salva simultaneamente em PostgreSQL e Redis |
| **Database per Service** | Todos | Cada serviço tem seu próprio schema PostgreSQL |
| **Domain Exception** | Todos | Exceções semânticas por domínio de negócio |
| **Global Error Handler** | Todos | `@RestControllerAdvice` centraliza o tratamento |
| **AOP Metrics** | Pessoa + Log | `@Metrica` registra counter e timer automaticamente |
| **Distributed Tracing** | Todos | TraceId/SpanId propagados via OpenTelemetry |

---

## 🔧 Tecnologias e Versões

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 21 | Linguagem de programação |
| Spring Boot | 3.5+ | Framework principal |
| Spring Data JPA | 3.5+ | Persistência de dados |
| Spring AMQP | 3.5+ | Integração com RabbitMQ |
| Spring Data Redis | 3.5+ | Integração com Redis |
| Spring AOP | 3.5+ | Interceptação para métricas |
| Spring Validation | 3.5+ | Validação de campos |
| RabbitMQ | 3.13 | Message broker |
| Redis | 7.2 | Cache distribuído |
| PostgreSQL | 15+ | Banco de dados relacional |
| Flyway | Latest | Versionamento de banco |
| Resilience4j | 2.2.0 | Circuit Breaker |
| Micrometer OTLP | Latest | Exportação de métricas |
| Micrometer Tracing | Latest | Distributed tracing |
| OpenTelemetry | Latest | Collector de telemetria |
| Grafana LGTM | Latest | Observabilidade (Mimir+Tempo+Loki) |
| Lettuce | Latest | Cliente Redis |
| Lombok | 1.18.32 | Redução de boilerplate |
| SpringDoc OpenAPI | 2.7.0 | Documentação da API |
| Jackson | Latest | Serialização JSON |
| Maven | 3.9+ | Gerenciamento de dependências |

---

## 🔄 Fluxos de Comunicação

### Fluxo 1 — Cadastro de Pessoa

```
1. Cliente envia POST /api/pessoa
2. PessoaService chama SerasaService
3. SerasaService publica CPF em verificar-serasa-request (timeout 3s)
4. SerasaConsumerService consome, verifica CPF, responde em verificar-serasa-response
5. PessoaService recebe Boolean negativado
   └── Se timeout/falha: Circuit Breaker retorna null (fallback)
6. Pessoa é salva no PostgreSQL com campo negativado
7. LogService publica evento em enviar-log (fire-and-forget)
8. LogConsumerService consome e salva em PostgreSQL + Redis
9. PessoaService retorna 201 para o cliente
```

### Fluxo 2 — Consulta de Logs (Cache-Aside)

```
1. Cliente envia GET /api/logs
2. LogService delega para LogCacheService
3. LogCacheService consulta Redis
   ├── Cache HIT  → retorna lista direto do Redis
   └── Cache MISS → busca PostgreSQL → popula Redis → retorna lista
4. LogController converte para LogResponseDto e retorna 200
```

### Fluxo 3 — Circuit Breaker em ação

```
CLOSED: chamada normal ao Serasa
  └── falha/timeout → OPEN: fallback imediato (negativado = null)
        └── após 3s → HALF_OPEN: testa uma chamada
              ├── sucesso → CLOSED: volta ao normal
              └── falha  → OPEN: permanece no fallback
```

---

## 🗂️ Microsserviços

### 🟢 microsservico-pessoa (porta 8090)

Responsável pelo CRUD de pessoas. Orquestra as integrações com Serasa (síncrono) e Log (assíncrono).

**Endpoints:**

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/pessoa` | Cadastrar pessoa |
| PUT | `/api/pessoa/{id}` | Atualizar pessoa |
| DELETE | `/api/pessoa/{id}` | Deletar pessoa |

**Validações:**
- Nome: obrigatório, 2–150 caracteres
- CPF: obrigatório, formato brasileiro válido, único no sistema
- Data de nascimento: obrigatória, deve ser no passado

**Documentação:** `http://localhost:8090/api/swagger-ui.html`

---

### 🟡 microsservico-serasa (porta 8070)

Simula consulta de negativação financeira via RabbitMQ (Request-Reply).

**CPFs negativados (mock):**
```
18142226006
16470435068
```

**Lógica:** CPF na lista → `true` (negativado) | CPF fora da lista → `false`

---

### 🔵 microsservico-log (porta 8060)

Consome eventos de auditoria e persiste com cache Redis.

**Endpoints:**

| Método | Endpoint | Descrição | Cache |
|--------|----------|-----------|-------|
| GET | `/api/logs` | Listar todos os logs | ✅ Cache-Aside |
| GET | `/api/logs/health` | Health check | ❌ |

**Estrutura de chaves Redis:**
```
log:log:123          → Log individual     (TTL: 24h)
log:all              → Lista de IDs       (TTL: 1h)
log:operation:CADASTRO   → Índice por operação
log:service:pessoa       → Índice por microsserviço
```

**Documentação:** `http://localhost:8060/api/swagger-ui.html`

---

## 📊 Observabilidade (OpenTelemetry + Grafana LGTM)

Todos os microsserviços exportam métricas, traces e logs para o Grafana LGTM via protocolo OTLP.

```
microsservico-pessoa  ─┐
microsservico-log     ─┼──► OTel Collector :4318 ──► Grafana LGTM :3000
                        │        ├── Mimir  (métricas)
                        │        ├── Tempo  (traces)
                        │        └── Loki   (logs)
```

### Métricas customizadas disponíveis

**microsservico-pessoa:**

| Métrica | Tipo |
|---------|------|
| `pessoa.cadastros.total` / `.duracao` | Counter / Timer |
| `pessoa.edicoes.total` / `.duracao` | Counter / Timer |
| `pessoa.exclusoes.total` / `.duracao` | Counter / Timer |
| `serasa.consultas.total` | Counter (sucesso/fallback) |
| `rabbitmq.mensagens.assincrono.total` | Counter |
| `rabbitmq.mensagens.sincrono.total` | Counter |
| `rabbitmq.sincrono.duracao` | Timer |

**microsservico-log:**

| Métrica | Tipo |
|---------|------|
| `log.cadastros.total` / `.duracao` | Counter / Timer |
| `log.consultas.total` / `.duracao` | Counter / Timer |

### Validando métricas pelo Actuator

```bash
# Pessoa
curl http://localhost:8090/api/actuator/metrics
curl http://localhost:8090/api/actuator/metrics/pessoa.cadastros.total

# Log
curl http://localhost:8060/api/actuator/metrics
curl http://localhost:8060/api/actuator/metrics/log.cadastros.duracao
```

> ⚠️ As métricas só aparecem após o método anotado com `@Metrica` ser executado ao menos uma vez.

### Distributed Tracing

Os logs de todos os serviços incluem `traceId` e `spanId` no padrão:
```
INFO  [microsservico-pessoa,<traceId>,<spanId>] ...
```

Isso permite correlacionar no Grafana Loki e navegar para o trace completo no Grafana Tempo.

---

## 🚀 Guia de Instalação e Execução

### Pré-requisitos

- **Java 21** — [Download](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** — [Download](https://www.docker.com/products/docker-desktop/)
- **PostgreSQL 15+** — [Download](https://www.postgresql.org/download/)

### 1. Infraestrutura

```bash
# RabbitMQ
docker compose -f rabbitmq/docker-compose.yml up -d

# Redis + RedisInsight
docker compose -f redis/docker-compose.yml up -d

# Grafana LGTM (métricas + traces + logs)
docker compose -f grafana/docker-compose.yml up -d
```

### 2. Banco de Dados (PostgreSQL)

```sql
-- Microsserviço Pessoa
CREATE DATABASE pessoa_db;
\c pessoa_db
CREATE SCHEMA pessoa_db;

-- Microsserviço Log
CREATE DATABASE logs_db;
\c logs_db
CREATE SCHEMA logs_db;
```

> O Flyway cria as tabelas automaticamente no startup de cada microsserviço.

### 3. Microsserviços (ordem recomendada)

```bash
# Terminal 1
cd microsservico-serasa && ./mvnw clean spring-boot:run

# Terminal 2
cd microsservico-log && ./mvnw clean spring-boot:run

# Terminal 3
cd microsservico-pessoa && ./mvnw clean spring-boot:run
```

### ✅ Verificação

| Serviço | URL | Status esperado |
|---------|-----|-----------------|
| microsservico-pessoa | http://localhost:8090/api | ✅ |
| microsservico-serasa | http://localhost:8070 | ✅ |
| microsservico-log | http://localhost:8060/api | ✅ |
| Swagger (Pessoa) | http://localhost:8090/api/swagger-ui.html | 📄 |
| Swagger (Log) | http://localhost:8060/api/swagger-ui.html | 📄 |
| RabbitMQ Management | http://localhost:15672 | 🐰 admin/admin |
| RedisInsight | http://localhost:5540 | 🔴 |
| Grafana | http://localhost:3000 | 📊 admin/admin |

---

## 🧪 Testando a Aplicação

### Cadastrar pessoa (CPF regular)

```bash
curl -X POST http://localhost:8090/api/pessoa \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Santos",
    "cpf": "12345678901",
    "dataNascimento": "1995-05-20"
  }'
```

### Cadastrar pessoa (CPF negativado)

```bash
curl -X POST http://localhost:8090/api/pessoa \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "cpf": "18142226006",
    "dataNascimento": "1988-03-15"
  }'
```

### Consultar logs (primeira vez — cache miss)

```bash
curl http://localhost:8060/api/logs
# Busca do PostgreSQL + popula Redis
```

### Consultar logs (segunda vez — cache hit)

```bash
curl http://localhost:8060/api/logs
# Retorna do Redis diretamente
```

### Verificar cache no Redis

```bash
docker exec -it redis redis-cli -a senha123

KEYS log:*              # Todas as chaves
GET log:log:1           # Log individual
LRANGE log:all 0 -1     # Lista de IDs
TTL log:log:1           # Tempo de expiração
```

---

## 🔒 Recomendações para Produção

- Implementar Spring Security com JWT / OAuth2
- Habilitar TLS no RabbitMQ e Redis
- Usar HashiCorp Vault para gestão de segredos — nunca commitar credenciais
- Reduzir `tracing.sampling.probability` para `0.1` (10%)
- Configurar Dead Letter Queue (DLQ) no RabbitMQ para mensagens com falha
- Implementar Redis Sentinel ou Cluster para alta disponibilidade
- Adicionar rate limiting com Bucket4j
- Configurar ACLs no Redis (desabilitar `FLUSHALL`, `CONFIG`)

---

## 🚀 Roadmap

- [ ] Service Discovery com Eureka ou Consul
- [ ] API Gateway com Spring Cloud Gateway
- [ ] Dead Letter Queue para reprocessamento de mensagens com falha
- [ ] Saga Pattern para transações distribuídas
- [ ] CQRS — separação de comandos e consultas
- [ ] Testes de integração com Testcontainers
- [ ] Dockerfiles e docker-compose completo para todos os serviços
- [ ] Kubernetes — Deployments, Services e Ingress
- [ ] CI/CD com GitHub Actions
- [ ] Cache Warming — pré-carregar Redis no startup
- [ ] Paginação nos endpoints de consulta

---

## 📚 Documentação por Microsserviço

Cada microsserviço possui seu próprio README detalhado:

- [`microsservico-pessoa/README.md`](microsservico-pessoa/README.md) — CRUD, Circuit Breaker, métricas
- [`microsservico-serasa/README.md`](microsservico-serasa/README.md) — Request-Reply, mock de CPFs
- [`microsservico-log/README.md`](microsservico-log/README.md) — Cache-Aside, Write-Through, OpenTelemetry

---

## 📈 Estatísticas do Projeto

```
├── 3 Microsserviços independentes
├── 2 Bancos PostgreSQL (schema por serviço)
├── 1 Message Broker (RabbitMQ)
├── 1 Cache distribuído (Redis)
├── 1 Stack de observabilidade (Grafana LGTM)
├── 2 Padrões de mensageria (síncrono + assíncrono)
├── 10+ Padrões arquiteturais implementados
├── Métricas customizadas com AOP
└── Distributed Tracing end-to-end
```