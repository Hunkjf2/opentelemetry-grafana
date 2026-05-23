# Microsserviço Pessoa

## 📋 Descrição
Microsserviço responsável pelo gerenciamento de pessoas no sistema. Realiza operações CRUD, integra com o **microsserviço Serasa** via comunicação síncrona (Request-Reply) para consulta de negativação, e envia eventos de auditoria de forma assíncrona (Fire-and-Forget) para o **microsserviço Log** — tudo via **RabbitMQ**.

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 21 | Linguagem de programação |
| Spring Boot | 3.5.3 | Framework principal |
| Spring Data JPA | 3.5.3 | Persistência no PostgreSQL |
| Spring AMQP | 3.5.3 | Integração com RabbitMQ |
| Spring Validation | 3.5.3 | Validação de campos |
| Spring AOP | 3.5.3 | Interceptação para métricas |
| PostgreSQL | 15+ | Banco de dados relacional |
| RabbitMQ | 3.13 | Message broker |
| Resilience4j | 2.2.0 | Circuit Breaker para tolerância a falhas |
| Flyway | Latest | Versionamento de banco |
| Micrometer + OTLP | Latest | Métricas e traces (OpenTelemetry) |
| Lombok | 1.18.32 | Redução de boilerplate |
| SpringDoc OpenAPI | 2.7.0 | Documentação da API |
| Jackson | Latest | Serialização JSON |
| Maven | 3.9+ | Gerenciamento de dependências |

---

## 🏗️ Estrutura do Projeto

```
src/main/java/com/example/pessoa/
├── PessoaApplication.java
├── config/
│   ├── cors/
│   │   └── CorsConfig.java                      # CORS liberado para todos as origens
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java           # Handler global com @RestControllerAdvice
│   │   ├── PessoaNaoEncontradaException.java     # 404 - pessoa não encontrada
│   │   ├── CpfJaCadastradoException.java         # 409 - CPF duplicado
│   │   └── ProcessingException.java              # 500 - falha no processamento RabbitMQ
│   ├── jackson/
│   │   └── ObjectMapperConfig.java
│   ├── metrics/
│   │   ├── Loggable.java                         # Anotação para log de operações
│   │   ├── Metrica.java                          # Anotação para métricas automáticas
│   │   └── MetricaAspect.java                    # Aspecto AOP (intercepta @Metrica)
│   ├── observability/
│   │   ├── ObservabilityConfig.java              # Tags globais no MeterRegistry
│   │   └── ObservacaoBancoDadosHandler.java      # Intercepta observações JDBC
│   ├── rabbitmq/
│   │   └── RabbitMQConfig.java                   # Filas, exchange, bindings e templates
│   └── swagger/
│       └── SwaggerConfig.java
├── constants/
│   ├── global/
│   │   └── MenssagemSistema.java
│   ├── log/
│   │   ├── Operacao.java                         # CADASTRO, ATUALIZAÇÃO, EXCLUSÃO
│   │   └── TopicLog.java                         # enviar-log
│   ├── pessoa/
│   │   └── Pessoa.java                           # Mensagens de validação de CPF
│   └── serasa/
│       └── TopicSerasa.java                      # verificar-serasa-request/response
├── controller/
│   └── PessoaController.java
├── dto/
│   ├── ErrorResponse.java
│   ├── LogEventDto.java
│   ├── PessoaDto.java
│   └── SuccessResponse.java
├── mapper/
│   └── PessoaMapper.java
├── model/
│   └── Pessoa.java
├── repository/
│   └── PessoaRepository.java
└── service/
    ├── log/
    │   └── LogService.java                       # Monta e envia evento de auditoria
    ├── metrics/
    │   └── MetricsService.java                   # Métricas manuais (Serasa, RabbitMQ)
    ├── pessoa/
    │   └── PessoaService.java                    # Orquestração de negócio
    ├── rabbitmq/
    │   ├── RabbitMQAssincronoService.java         # Fire-and-Forget
    │   └── RabbitMQSincronoService.java           # Request-Reply com timeout
    └── serasa/
        └── SerasaService.java                    # Consulta com Circuit Breaker

src/main/resources/
├── application.yml
└── db/migration/
    └── V1__create_table_pessoa.sql
```

---

## 🏛️ Arquitetura e Fluxos de Comunicação

### Fluxo completo de cadastro

```
Cliente HTTP
    │
    ▼ POST /api/pessoa
PessoaController
    │
    ▼
PessoaService
    ├── 1. SerasaService ──► RabbitMQSincronoService
    │         │                      │ verificar-serasa-request
    │         │                      ▼
    │         │              [microsservico-serasa]
    │         │                      │ verificar-serasa-response
    │         ◄──────────────────────┘
    │         Boolean negativado (ou null se Circuit Breaker abriu)
    │
    ├── 2. PessoaRepository.save() ──► PostgreSQL
    │
    └── 3. LogService ──► RabbitMQAssincronoService
                                  │ enviar-log (fire-and-forget)
                                  ▼
                          [microsservico-log]
```

### Padrões de mensageria utilizados

| Padrão | Uso | Fila | Timeout |
|--------|-----|------|---------|
| Request-Reply (síncrono) | Consulta Serasa | `verificar-serasa-request` / `verificar-serasa-response` | 3s |
| Fire-and-Forget (assíncrono) | Envio de log | `enviar-log` | — |

---

## 🛡️ Tratamento de Erros

Todo o tratamento está centralizado no `GlobalExceptionHandler` com `@RestControllerAdvice`.

| Exceção | HTTP Status | Quando ocorre |
|---------|-------------|---------------|
| `PessoaNaoEncontradaException` | 404 | ID não existe no banco |
| `CpfJaCadastradoException` | 409 | CPF já pertence a outra pessoa |
| `MethodArgumentNotValidException` | 400 | Falha nas validações de campo |
| `ProcessingException` | 500 | Falha no envio/recebimento RabbitMQ |
| `Exception` (genérica) | 500 | Qualquer erro inesperado |

**Exemplo de resposta de erro:**
```json
{
  "status": 404,
  "message": "Não existe este registro na base de dados."
}
```

**Exemplo de erro de validação:**
```json
{
  "status": 400,
  "message": "Erro de validação",
  "errors": {
    "nome": "Nome é obrigatório",
    "cpf": "CPF deve ter formato válido"
  }
}
```

---

## ⚡ Circuit Breaker (Resilience4j)

Protege a chamada ao microsserviço Serasa. Se o serviço estiver indisponível ou o timeout de 3s for atingido, o Circuit Breaker abre e o fallback retorna `null` para o campo `negativado`.

```yaml
resilience4j:
  circuitbreaker:
    instances:
      microsservico-serasa:
        minimum-number-of-calls: 1
        failure-rate-threshold: 100
        wait-duration-in-open-state: 3s
        automatic-transition-from-open-to-half-open-enabled: true
```

**Estados do Circuit Breaker:**

```
CLOSED ──(falha)──► OPEN ──(3s)──► HALF_OPEN ──(sucesso)──► CLOSED
                                         └──(falha)──► OPEN
```

| Estado | Comportamento |
|--------|--------------|
| CLOSED | Chamadas normais ao Serasa |
| OPEN | Fallback imediato, não chama o Serasa |
| HALF_OPEN | Testa uma chamada para verificar recuperação |

**Fallback:**
```java
public Boolean fallbackConsultarSituacaoFinanceira(String cpf, Exception ex) {
    // negativado = null → pessoa cadastrada sem informação de crédito
    return null;
}
```

---

## 📊 Métricas com OpenTelemetry

### Como funciona o `@Metrica`

A anotação `@Metrica` intercepta métodos via AOP (disponível transitivamente pelo Resilience4j) e registra automaticamente dois indicadores no Micrometer:

- **Counter** (`nome.total`) — incrementa a cada execução, com tag `resultado=sucesso|erro`
- **Timer** (`nome.duracao`) — mede o tempo de execução, com tags `operacao` e `resultado`

```java
@Metrica(
    nome = "pessoa.cadastros",
    descricao = "Cadastros de pessoa",
    operacao = "cadastrar"
)
public Pessoa cadastrar(PessoaDto pessoaDto) { ... }
```

> ℹ️ O `spring-boot-starter-aop` não precisa ser declarado explicitamente aqui pois já vem
> como dependência transitiva do `resilience4j-spring-boot3`.

### Métricas disponíveis

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `pessoa.cadastros.total` | Counter | Total de cadastros (sucesso/erro) |
| `pessoa.cadastros.duracao` | Timer | Duração do cadastro |
| `pessoa.edicoes.total` | Counter | Total de edições (sucesso/erro) |
| `pessoa.edicoes.duracao` | Timer | Duração da edição |
| `pessoa.exclusoes.total` | Counter | Total de exclusões (sucesso/erro) |
| `pessoa.exclusoes.duracao` | Timer | Duração da exclusão |
| `serasa.consultas.total` | Counter | Consultas ao Serasa (sucesso/fallback) |
| `rabbitmq.mensagens.assincrono.total` | Counter | Mensagens fire-and-forget enviadas |
| `rabbitmq.mensagens.sincrono.total` | Counter | Mensagens Request-Reply enviadas |
| `rabbitmq.sincrono.duracao` | Timer | Duração das chamadas síncronas RabbitMQ |

### Validando as métricas

> ⚠️ A métrica só aparece no Actuator após o método ser executado ao menos uma vez.

**1. Liste todas as métricas:**
```bash
curl http://localhost:8090/api/actuator/metrics
```

**2. Consulte métricas específicas:**
```bash
curl http://localhost:8090/api/actuator/metrics/pessoa.cadastros.total
curl http://localhost:8090/api/actuator/metrics/pessoa.cadastros.duracao
curl http://localhost:8090/api/actuator/metrics/serasa.consultas.total
curl http://localhost:8090/api/actuator/metrics/rabbitmq.sincrono.duracao
```

**3. Valide no Grafana (Mimir/Prometheus):**
```promql
pessoa_cadastros_total{resultado="sucesso"}
serasa_consultas_total{resultado="fallback"}
rabbitmq_sincrono_duracao_seconds_sum
```

### Observabilidade JDBC

O `ObservacaoBancoDadosHandler` intercepta todas as observações JDBC e emite logs estruturados com `traceId` e `spanId` correlacionados ao Grafana Tempo:

```
[ JDBC ] >> INICIO    | operacao='jdbc.query' | detalhes=...
[ JDBC ] OK CONCLUSAO | operacao='jdbc.query' | detalhes=...
[ JDBC ] XX ERRO      | operacao='jdbc.query' | causa='...'
```

---

## 🔧 Configuração

### Banco de Dados
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pessoa_db?currentSchema=pessoa_db
    username: postgres
    password: postgresql
  flyway:
    baseline-on-migrate: true
    schemas: pessoa_db
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

### Circuit Breaker
```yaml
resilience4j:
  circuitbreaker:
    instances:
      microsservico-serasa:
        minimum-number-of-calls: 1
        failure-rate-threshold: 100
        wait-duration-in-open-state: 3s
        automatic-transition-from-open-to-half-open-enabled: true
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

logging:
  pattern:
    level: "%5p [${spring.application.name},%X{traceId:-},%X{spanId:-}]"
```

---

## 🚀 Como Executar

### Pré-requisitos
- Java 21
- PostgreSQL rodando na porta 5432
- RabbitMQ rodando na porta 5672
- Microsserviço Serasa rodando na porta 8070
- Microsserviço Log rodando na porta 8060
- Grafana LGTM (opcional, para observabilidade)

### Infraestrutura via Docker

```bash
# RabbitMQ
docker compose -f rabbitmq/docker-compose.yml up -d

# Grafana LGTM (métricas + traces + logs)
docker compose -f grafana/docker-compose.yml up -d
```

### Aplicação

```bash
cd microsservico-pessoa
./mvnw clean spring-boot:run
```

O serviço estará disponível em: `http://localhost:8090/api`

### Ordem recomendada de inicialização

```
1. PostgreSQL
2. RabbitMQ
3. microsservico-serasa   (porta 8070)
4. microsservico-log      (porta 8060)
5. microsservico-pessoa   (porta 8090)  ← este
```

---

## 🌐 Endpoints REST

| Método | Endpoint | Descrição | Status de sucesso |
|--------|----------|-----------|-------------------|
| POST | `/api/pessoa` | Cadastrar nova pessoa | 201 Created |
| PUT | `/api/pessoa/{id}` | Atualizar dados de pessoa | 201 Created |
| DELETE | `/api/pessoa/{id}` | Remover pessoa | 200 OK |
| GET | `/api/actuator/metrics` | Todas as métricas | 200 OK |
| GET | `/api/actuator/health` | Saúde da aplicação | 200 OK |

**Swagger UI:** `http://localhost:8090/api/swagger-ui.html`

---

## 📊 Modelo de Dados

### Tabela `pessoa`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | BIGSERIAL | Chave primária |
| nome | VARCHAR(150) | Nome completo |
| cpf | VARCHAR(11) | CPF único (validado) |
| data_nascimento | DATE | Data de nascimento |
| negativado | BOOLEAN | Status Serasa (null se Circuit Breaker abriu) |
| data_hora_criacao | TIMESTAMP | Preenchido automaticamente pelo banco |

### Validações de entrada (`PessoaDto`)

| Campo | Regra |
|-------|-------|
| `nome` | Obrigatório, entre 2 e 150 caracteres |
| `cpf` | Obrigatório, formato CPF válido (validação brasileira) |
| `dataNascimento` | Obrigatória, deve ser no passado |

---

## 🔌 Integração com outros microsserviços

### Microsserviço Serasa (síncrono)

| Item | Valor |
|------|-------|
| Fila de envio | `verificar-serasa-request` |
| Fila de retorno | `verificar-serasa-response` |
| Exchange | `serasa.exchange` (Direct) |
| Timeout | 3 segundos |
| Fallback | `null` (Circuit Breaker) |

### Microsserviço Log (assíncrono)

| Item | Valor |
|------|-------|
| Fila | `enviar-log` |
| Padrão | Fire-and-Forget |
| Operações auditadas | CADASTRO, ATUALIZAÇÃO, EXCLUSÃO |

**Formato do evento enviado:**
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

## 🧪 Exemplos de uso

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

**Response 201:**
```json
{
  "id": 1,
  "nome": "Maria Santos",
  "cpf": "12345678901",
  "dataNascimento": "1995-05-20",
  "negativado": false,
  "dataHoraCriacao": "2025-10-27T10:30:00"
}
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

**Response 201:**
```json
{
  "id": 2,
  "nome": "João Silva",
  "cpf": "18142226006",
  "dataNascimento": "1988-03-15",
  "negativado": true,
  "dataHoraCriacao": "2025-10-27T10:32:00"
}
```

### Atualizar pessoa

```bash
curl -X PUT http://localhost:8090/api/pessoa/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Santos Silva",
    "cpf": "12345678901",
    "dataNascimento": "1995-05-20"
  }'
```

### Deletar pessoa

```bash
curl -X DELETE http://localhost:8090/api/pessoa/1
```

**Response 200:**
```json
{
  "status": 200,
  "message": "Operação realizada com sucesso."
}
```

---

## 🔍 Observabilidade

### Acessos

| Ferramenta | URL | Credenciais |
|------------|-----|-------------|
| Grafana | http://localhost:3000 | admin / admin |
| RabbitMQ Management | http://localhost:15672 | admin / admin |
| Swagger UI | http://localhost:8090/api/swagger-ui.html | — |
| Actuator | http://localhost:8090/api/actuator | — |