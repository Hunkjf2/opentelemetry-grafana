# Microsserviço Log

## 📋 Descrição
Microsserviço responsável pelo gerenciamento de logs de auditoria no sistema. Consome mensagens de eventos de outros microsserviços via **RabbitMQ** e armazena informações de auditoria no **PostgreSQL**, com camada de cache distribuído utilizando **Redis** (estratégia Cache-Aside + Write-Through).

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 21 | Linguagem de programação |
| Spring Boot | 3.5.0 | Framework principal |
| Spring Data JPA | 3.5.0 | Persistência no PostgreSQL |
| Spring AMQP | 3.5.0 | Integração com RabbitMQ |
| Spring Data Redis | 3.5.0 | Integração com Redis |
| Spring AOP | 3.5.0 | Interceptação para métricas |
| PostgreSQL | 15+ | Banco de dados relacional |
| Redis | 7.2 | Cache distribuído |
| RabbitMQ | 3.13 | Message broker |
| Flyway | Latest | Versionamento de banco |
| Micrometer + OTLP | Latest | Métricas e traces (OpenTelemetry) |
| Lettuce | Latest | Cliente Redis recomendado |
| Lombok | 1.18.32 | Redução de boilerplate |
| SpringDoc OpenAPI | 2.7.0 | Documentação da API |
| Jackson | Latest | Serialização JSON |
| Maven | 3.9+ | Gerenciamento de dependências |

---

## 🏗️ Estrutura do Projeto

```
src/main/java/com/example/log/
├── LogApplication.java
├── config/
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java     # Handler global com @RestControllerAdvice
│   │   ├── LogCadastroException.java        # Exceção de domínio para cadastro
│   │   ├── LogConsultaException.java        # Exceção de domínio para consulta
│   │   └── ProcessingException.java         # Exceção de processamento RabbitMQ
│   ├── jackson/
│   │   └── ObjectMapperConfig.java
│   ├── metrics/
│   │   ├── Loggable.java                    # Anotação para log de operações
│   │   ├── Metrica.java                     # Anotação para métricas automáticas
│   │   └── MetricaAspect.java               # Aspecto AOP (intercepta @Metrica)
│   ├── rabbitmq/
│   │   └── RabbitMQConfig.java
│   └── redis/
│       ├── RedisConfig.java
│       └── RedisProperties.java
├── constants/
│   ├── RedisKeys.java
│   └── TopicLog.java
├── controller/
│   └── LogController.java
├── dto/
│   ├── LogDto.java
│   ├── LogEventDto.java
│   ├── LogResponseDto.java
│   └── PessoaDto.java
├── model/
│   └── Log.java
├── repository/
│   └── LogRepository.java
└── service/
    ├── log/
    │   ├── LogCacheService.java             # Estratégia Cache-Aside isolada
    │   ├── LogConsumerService.java          # Consumidor RabbitMQ
    │   └── LogService.java                 # Orquestração de negócio
    └── redis/
        ├── LogRedisService.java
        └── RedisService.java

src/main/resources/
├── application.yml
└── db/migration/
    └── V1__create_table_log.sql
```

---

## 🏛️ Arquitetura de Serviços

```
┌─────────────────────┐     RabbitMQ      ┌──────────────────────┐
│  microsservico-     │ ─── enviar-log ──► │  microsservico-log   │
│  pessoa             │   (fire-and-forget) │                      │
└─────────────────────┘                    │  ┌────────────────┐  │
                                           │  │ LogConsumer    │  │
                                           │  │ Service        │  │
                                           │  └───────┬────────┘  │
                                           │          │            │
                                           │  ┌───────▼────────┐  │
                                           │  │  LogService    │  │
                                           │  └───────┬────────┘  │
                                           │          │            │
                                           │    ┌─────┴──────┐    │
                                           │    │            │    │
                                           │  PostgreSQL   Redis   │
                                           └──────────────────────┘
```

---

## 🔄 Padrões de Cache Implementados

### Write-Through (Escrita)
Ao receber um evento via RabbitMQ, o log é salvo simultaneamente no PostgreSQL e no Redis, garantindo consistência imediata.

```
RabbitMQ → LogConsumerService → LogService → PostgreSQL ✓
                                           → Redis       ✓
```

### Cache-Aside (Leitura)
Isolado no `LogCacheService`, responsável por toda a decisão de onde buscar os dados:

```
GET /logs → LogService → LogCacheService → Redis (hit) → retorna
                                        → Redis (miss) → PostgreSQL → popula Redis → retorna
```

---

## 🛡️ Tratamento de Erros

Toda a lógica de tratamento está centralizada no `GlobalExceptionHandler` usando `@RestControllerAdvice` com `ProblemDetail` (RFC 9457 — padrão Spring 6+).

Não há try/catch espalhados nos serviços. Os serviços traduzem exceções técnicas em exceções de domínio semânticas:

| Exceção | HTTP Status | Quando ocorre |
|---------|-------------|---------------|
| `LogCadastroException` | 500 | Falha ao persistir log |
| `LogConsultaException` | 500 | Falha ao buscar logs |
| `ProcessingException` | 422 | Falha no processamento RabbitMQ |
| `Exception` (genérica) | 500 | Qualquer erro inesperado |

**Exemplo de resposta de erro (ProblemDetail):**
```json
{
  "type": "about:blank",
  "title": "Erro ao cadastrar log",
  "status": 500,
  "detail": "Falha ao persistir log no banco de dados",
  "timestamp": "2025-10-27T10:30:00Z"
}
```

---

## 📊 Métricas com OpenTelemetry

### Dependências necessárias no `pom.xml`

```xml
<!-- AOP obrigatório para o @Metrica funcionar (não vem transitivo sem Resilience4j) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- Actuator: expõe /actuator/metrics -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Exporta métricas via OTLP para o Grafana -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-otlp</artifactId>
</dependency>

<!-- Propaga traces via OpenTelemetry SDK -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>

<!-- Exporta traces para o Grafana Tempo -->
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>

<!-- Captura métricas e traces por query SQL -->
<dependency>
    <groupId>net.ttddyy.observation</groupId>
    <artifactId>datasource-micrometer-spring-boot</artifactId>
    <version>1.0.5</version>
</dependency>
```

> ⚠️ **Atenção:** `spring-boot-starter-aop` deve ser declarado explicitamente neste microsserviço.
> No microsserviço-pessoa ele vem transitivo pelo Resilience4j. Aqui não existe essa dependência,
> portanto sem declará-lo o `@Aspect` é ignorado silenciosamente pelo Spring.

### Como funciona o `@Metrica`

A anotação `@Metrica` intercepta métodos via AOP e registra automaticamente dois indicadores no Micrometer:

- **Counter** (`nome.total`) — incrementa a cada execução, com tag `resultado=sucesso|erro`
- **Timer** (`nome.duracao`) — mede o tempo de execução, com tags `operacao` e `resultado`

```java
@Metrica(
    nome = "log.cadastros",
    descricao = "Cadastros de Logs",
    operacao = "cadastro"
)
public void cadastrarLog(Log logPayload) { ... }
```

> ⚠️ **Importante:** a métrica só aparece no Actuator após o método ser executado ao menos uma vez.
> O Micrometer registra sob demanda, não no startup.

### Métricas disponíveis

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `log.cadastros.total` | Counter | Total de cadastros de log (sucesso/erro) |
| `log.cadastros.duracao` | Timer | Duração do cadastro de log |
| `log.consultas.total` | Counter | Total de consultas (sucesso/erro) |
| `log.consultas.duracao` | Timer | Duração da consulta de logs |

### Validando as métricas

**1. Liste todas as métricas expostas:**
```bash
curl http://localhost:8060/api/actuator/metrics
```

**2. Dispare ao menos uma execução** (envie uma mensagem pelo RabbitMQ ou cadastre uma pessoa no microsserviço-pessoa).

**3. Consulte a métrica específica:**
```bash
curl http://localhost:8060/api/actuator/metrics/log.cadastros.duracao
curl http://localhost:8060/api/actuator/metrics/log.cadastros.total
curl http://localhost:8060/api/actuator/metrics/log.consultas.duracao
```

**4. Valide no Grafana (Mimir/Prometheus):**
```promql
log_cadastros_total{resultado="sucesso"}
log_cadastros_duracao_seconds_sum
```

---

## 🔧 Configuração

### Banco de Dados
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/logs_db?currentSchema=logs_db
    username: postgres
    password: postgresql
  flyway:
    baseline-on-migrate: true
    default-schema: logs_db
    schemas: logs_db
```

### RabbitMQ
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin
```

### Redis
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: senha123
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2

app:
  redis:
    key-prefix: "log:"
    default-ttl: 86400   # 24 horas (logs individuais)
    list-ttl: 3600        # 1 hora (lista de IDs)
    max-list-size: 1000
```

### OpenTelemetry
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
  otlp:
    metrics:
      export:
        url: http://localhost:4318/v1/metrics
        step: 10s
    tracing:
      endpoint: http://localhost:4318/v1/traces
  tracing:
    sampling:
      probability: 1.0   # 100% em dev; use ~0.1 em produção
  metrics:
    tags:
      environment: local
```

---

## 🚀 Como Executar

### Pré-requisitos
- Java 21
- PostgreSQL rodando na porta 5432
- RabbitMQ rodando na porta 5672
- Redis rodando na porta 6379
- Grafana LGTM (opcional, para observabilidade)

### Infraestrutura via Docker

```bash
# RabbitMQ
docker compose -f rabbitmq/docker-compose.yml up -d

# Redis + RedisInsight
docker compose -f redis/docker-compose.yml up -d

# Grafana LGTM (métricas + traces + logs)
docker compose -f grafana/docker-compose.yml up -d
```

### Aplicação

```bash
cd microsservico-log
./mvnw clean spring-boot:run
```

O serviço estará disponível em: `http://localhost:8060/api`

---

## 🌐 Endpoints REST

| Método | Endpoint | Descrição | Cache |
|--------|----------|-----------|-------|
| GET | `/api/logs` | Listar todos os logs | ✅ Cache-Aside Redis |
| GET | `/api/logs/health` | Health check | ❌ |
| GET | `/api/actuator/metrics` | Todas as métricas | ❌ |
| GET | `/api/actuator/metrics/{nome}` | Métrica específica | ❌ |
| GET | `/api/actuator/health` | Saúde da aplicação | ❌ |

---

## 📊 Modelo de Dados

### Tabela `log`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | BIGSERIAL | Chave primária |
| id_usuario | BIGINT | ID do usuário que executou a operação |
| nome_usuario | VARCHAR(100) | Nome do usuário |
| operacao | VARCHAR(30) | Tipo: CADASTRO, ATUALIZAÇÃO, EXCLUSÃO |
| dados | TEXT | Evento completo serializado em JSON |
| nome_microsservico | VARCHAR(60) | Microsserviço que gerou o log |
| data_hora_criacao | TIMESTAMP | Data e hora da criação |

### Estrutura de Chaves Redis

```
log:log:123          → Log individual (TTL: 24h)
log:all              → Lista de IDs de todos os logs (TTL: 1h)
log:operation:CADASTRO   → Índice por operação
log:service:pessoa       → Índice por microsserviço
```

---

## 🔌 Integração via RabbitMQ

### Fila consumida

| Fila | Padrão | Descrição |
|------|--------|-----------|
| `enviar-log` | Fire-and-Forget | Recebe eventos de auditoria dos outros microsserviços |

### Formato do evento recebido

```json
{
  "pessoaDto": {
    "id": 1,
    "nome": "João Silva",
    "cpf": "12345678901",
    "dataNascimento": "1990-01-15",
    "negativado": false,
    "dataHoraCriacao": "2025-10-27T10:30:00"
  },
  "operacao": "CADASTRO",
  "microservico": "microservico-pessoa",
  "idUsuario": 1,
  "nomeUsuario": "Jhon Doe"
}
```

---

## 🔍 Observabilidade

### Acessos

| Ferramenta | URL | Credenciais |
|------------|-----|-------------|
| Grafana | http://localhost:3000 | admin / admin |
| RedisInsight | http://localhost:5540 | — |
| RabbitMQ Management | http://localhost:15672 | admin / admin |
| Swagger UI | http://localhost:8060/api/swagger-ui.html | — |

### Verificando cache no Redis

```bash
docker exec -it redis redis-cli -a senha123

KEYS log:*                        # Ver todas as chaves
GET log:log:1                     # Log individual
LRANGE log:all 0 -1               # Lista de todos os IDs
TTL log:log:1                     # Tempo de expiração
```