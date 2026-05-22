# Microsserviço Pessoa

## 📋 Descrição
Microsserviço responsável pelo gerenciamento de pessoas no sistema. Realiza operações CRUD para entidades Pessoa e integra com outros serviços através de mensageria Kafka para consulta no Serasa e envio de logs de auditoria.

## 🛠️ Tecnologias Utilizadas
- **Java 21**
- **Spring Boot 3.5.3**
- **Spring Data JPA**
- **PostgreSQL**
- **Spring Kafka**
- **Flyway** (migrações de banco)
- **Lombok**
- **SpringDoc OpenAPI** (Swagger)
- **Resilience4j** (Circuit Breaker)
- **Bean Validation**
- **Maven**

## 🏗️ Estrutura do Projeto

```
src/main/java/com/example/pessoa/
├── PessoaApplication.java           # Classe principal da aplicação
├── config/
│   ├── exception/
│   │   ├── ExceptionHandler.java    # Utilitário para tratamento de exceções
│   │   ├── GlobalExceptionHandler.java # Manipulador global de exceções
│   │   ├── PessoaNaoEncontradaException.java # Exceção customizada
│   │   ├── CpfJaCadastradoException.java # Exceção para CPF duplicado
│   │   └── ProcessingException.java # Exceção de processamento
│   ├── jackson/
│   │   └── ObjectMapperConfig.java  # Configuração do Jackson
│   ├── kafka/
│   │   └── KafkaConfig.java         # Configuração Kafka (Producer/Consumer)
│   └── swagger/
│       └── SwaggerConfig.java       # Configuração do Swagger
├── constants/
│   ├── global/
│   │   └── MenssagemSistema.java   # Mensagens do sistema
│   ├── log/
│   │   ├── Operacao.java           # Constantes de operações de log
│   │   └── TopicLog.java           # Tópicos de log
│   ├── pessoa/
│   │   └── Pessoa.java             # Constantes relacionadas a pessoa
│   └── serasa/
│       └── TopicSerasa.java        # Tópicos do Serasa
├── controller/
│   └── PessoaController.java       # Controller REST para pessoas
├── dto/
│   ├── ErrorResponse.java          # DTO para respostas de erro
│   ├── LogEventDto.java            # DTO para eventos de log
│   └── PessoaDto.java              # DTO da entidade pessoa
├── mapper/
│   └── PessoaMapper.java           # Mapeador entre DTO e entidade
├── model/
│   └── Pessoa.java                 # Entidade JPA pessoa
├── repository/
│   └── PessoaRepository.java       # Repositório JPA para pessoas
└── service/
    ├── kafka/
    │   ├── KafkaAssincronoService.java     # Serviço Kafka assíncrono
    │   ├── KafkaSerializationService.java  # Serviço de serialização
    │   └── KafkaSincronoService.java       # Serviço Kafka síncrono
    ├── log/
    │   └── LogService.java                 # Serviço de envio de logs
    ├── pessoa/
    │   └── PessoaService.java              # Serviço de negócio de pessoas
    └── serasa/
        └── SerasaService.java              # Serviço de integração Serasa

src/main/resources/
├── application.yml                  # Configurações da aplicação
└── db/migration/
    └── V1__create_table_pessoa.sql # Script de criação da tabela pessoa
```

## 🔧 Configuração

### Banco de Dados
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/pessoa_db?currentSchema=pessoa_db
    username: postgresql
    password: postgresql
```

### Kafka
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      retries: 3
    consumer:
      group-id: pessoa-reply-group
      auto-offset-reset: earliest
```

### Circuit Breaker
```yaml
resilience4j:
  circuitbreaker:
    instances:
      microsservico-serasa:
        minimum-number-of-calls: 3
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        automatic-transition-from-open-to-half-open-enabled: true
```

## 🚀 Como Executar

### Pré-requisitos
- Java 21
- PostgreSQL (porta 5433)
- Apache Kafka (porta 9092)
- Maven

### Passos
1. Configure o banco PostgreSQL na porta 5433
2. Configure o Kafka na porta 9092
3. Execute o comando:
```bash
./mvnw spring-boot:run
```

O serviço estará disponível em: `http://localhost:8090`

## 📊 Modelo de Dados

### Tabela `pessoa`
| Campo | Tipo | Descrição |
|-------|------|-----------|
| id | BIGSERIAL | Chave primária |
| nome | VARCHAR(150) | Nome da pessoa |
| cpf | VARCHAR(11) | CPF (validado e único) |
| data_nascimento | DATE | Data de nascimento |
| negativado | BOOLEAN | Status de negativação (consultado no Serasa) |
| data_hora_criacao | TIMESTAMP | Data e hora da criação (automática) |

### Script de Criação
```sql
CREATE TABLE pessoa (
     id bigserial NOT NULL,
     nome varchar(150) NOT NULL,
     cpf varchar(11) UNIQUE NOT NULL,
     data_nascimento date NOT NULL,
     negativado boolean NULL,
     data_hora_criacao timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
     CONSTRAINT pessoa_pkey PRIMARY KEY (id)
);
```

## 🌐 API Endpoints

### Swagger UI
- **URL**: `http://localhost:8090/swagger-ui.html`
- **API Docs**: `http://localhost:8090/v3/api-docs`

### Endpoints Disponíveis

#### 1. Cadastrar Pessoa
- **Endpoint**: `POST /api/pessoa`
- **Descrição**: Cadastra uma nova pessoa no sistema
- **Request Body**:
```json
{
  "nome": "João Silva",
  "cpf": "11111111111",
  "dataNascimento": "1990-01-01"
}
```

#### 2. Atualizar Pessoa
- **Endpoint**: `PUT /api/pessoa/{id}`
- **Descrição**: Atualiza os dados de uma pessoa existente
- **Request Body**:
```json
{
  "nome": "João Silva Santos",
  "cpf": "11111111111",
  "dataNascimento": "1990-01-01"
}
```

#### 3. Deletar Pessoa
- **Endpoint**: `DELETE /api/pessoa/{id}`
- **Descrição**: Remove uma pessoa do sistema


## 🔄 Integração com Outros Serviços

### Serviço Serasa (Comunicação Síncrona)
- **Tópico Request**: `verificar-serasa-request`
- **Tópico Response**: `verificar-serasa-response`
- **Timeout**: 3 segundos
- **Circuit Breaker**: Ativado com fallback
- **Finalidade**: Verificar situação financeira durante o cadastro

### Serviço de Log (Comunicação Assíncrona)
- **Tópico**: `enviar-log`
- **Operações Logadas**:
    - `CADASTRO`
    - `ATUALIZAÇÃO`
    - `EXCLUSÃO`

## ✅ Validações

### Validações de Campo
- **Nome**:
    - Obrigatório
    - Entre 2 e 150 caracteres
- **CPF**:
    - Obrigatório
    - Formato válido (validação brasileira)
    - Máximo 11 caracteres
    - Único no sistema
- **Data de Nascimento**:
    - Obrigatória
    - Deve ser no passado

### Exemplo de Erro de Validação
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

## 🛡️ Tratamento de Erros

### Exceções Customizadas
- **PessoaNaoEncontradaException**: HTTP 404
- **CpfJaCadastradoException**: HTTP 409
- **ProcessingException**: HTTP 500

### Respostas de Erro Padronizadas
```json
{
  "status": 404,
  "message": "Não existe este registro na base de dados."
}
```

## 🔄 Fluxo de Negócio

### Cadastro de Pessoa
1. Recebe dados via POST
2. Valida campos obrigatórios
3. Verifica se CPF já existe
4. Consulta situação no Serasa (síncrono)
5. Salva pessoa no banco
6. Envia log de auditoria (assíncrono)
7. Retorna dados da pessoa criada

### Atualização de Pessoa
1. Busca pessoa por ID
2. Valida se CPF não pertence a outra pessoa
3. Atualiza dados (nome e data nascimento)
4. Salva no banco
5. Envia log de auditoria
6. Retorna dados atualizados

### Exclusão de Pessoa
1. Busca pessoa por ID
2. Remove do banco
3. Envia log de auditoria

## 🔧 Configurações Adicionais

### Logs
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.apache.kafka: INFO
    org.springframework.kafka.requestreply.ReplyingKafkaTemplate: OFF
```

### Migrations
As migrações são executadas automaticamente pelo Flyway:
- `V1__create_table_pessoa.sql` - Criação da tabela pessoa

