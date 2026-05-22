# Microsserviço Log

## 📋 Descrição
Microsserviço responsável pelo gerenciamento de logs de auditoria no sistema. Consome mensagens de eventos de outros microsserviços e armazena informações de auditoria no banco de dados PostgreSQL de forma assíncrona.

## 🛠️ Tecnologias Utilizadas
- **Java 21**
- **Spring Boot 3.5.0**
- **Spring Data JPA**
- **PostgreSQL**
- **Spring Kafka**
- **Flyway** (migrações de banco)
- **Lombok**
- **Jackson** (processamento JSON)
- **Maven**

## 🏗️ Estrutura do Projeto

```
src/main/java/com/example/log/
├── LogApplication.java              # Classe principal da aplicação
├── config/
│   ├── jackson/
│   │   └── ObjectMapperConfig.java  # Configuração do Jackson para JSON
│   └── kafka/
│       └── KafkaConfig.java         # Configuração do Kafka Consumer
├── constants/
│   └── TopicLog.java                # Constantes dos tópicos Kafka
├── dto/
│   ├── LogDto.java                  # DTO para transferência de dados de log
│   ├── LogEventDto.java             # DTO para eventos de log recebidos
│   └── PessoaDto.java               # DTO da entidade pessoa (para deserialização)
├── model/
│   └── Log.java                     # Entidade JPA para logs
├── repository/
│   └── LogRepository.java           # Repositório JPA para logs
└── service/
    ├── LogConsumerService.java      # Serviço consumidor Kafka
    └── LogService.java              # Serviço de negócio para logs

src/main/resources/
├── application.yml                  # Configurações da aplicação
└── db/migration/
    └── V1__create_table_log.sql     # Script de criação da tabela log
```

## 🔧 Configuração

### Banco de Dados
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/log_db?currentSchema=log_db
    username: postgresql
    password: postgresql
```

### Kafka
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: log-service-group
      auto-offset-reset: earliest
      enable-auto-commit: true
```

### Flyway
```yaml
spring:
  flyway:
    baseline-on-migrate: true
    baseline-version: 0
    default-schema: log_db
    schemas: log_db
```

## 🚀 Como Executar

### Pré-requisitos
- Java 21
- PostgreSQL (porta 5432)
- Apache Kafka (porta 9092)
- Maven

### Passos
1. Configure o banco PostgreSQL na porta 5432
2. Configure o Kafka na porta 9092
3. Execute o comando:
```bash
./mvnw spring-boot:run
```

O serviço estará disponível em: `http://localhost:8060`

## 📊 Modelo de Dados

### Tabela `log`
| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | BIGSERIAL | Chave primária |
| id_usuario | BIGINT | ID do usuário que executou a operação |
| nome_usuario | VARCHAR(100) | Nome do usuário |
| operacao | VARCHAR(30) | Tipo de operação (CADASTRO, ATUALIZAÇÃO, EXCLUSÃO) |
| dados | TEXT | Dados completos da operação em JSON |
| nome_microsservico | VARCHAR(60) | Nome do microsserviço que gerou o log |
| data_hora_criacao | TIMESTAMP | Data e hora da criação do log |

### Script de Criação
```sql
CREATE TABLE log (
    id BIGSERIAL PRIMARY KEY,
    id_usuario bigint NOT NULL,
    nome_usuario VARCHAR(100) NOT NULL,
    operacao VARCHAR(30) NOT NULL,
    dados TEXT NOT NULL,
    nome