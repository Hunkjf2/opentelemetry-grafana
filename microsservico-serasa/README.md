# Microsserviço Serasa

## Descrição
Microsserviço responsável por simular consultas de negativação no Serasa. Processa solicitações síncronas de verificação de CPF via Kafka e retorna informações sobre a situação financeira da pessoa baseada em uma base de dados mockada.

## Tecnologias Utilizadas
- **Java 21**
- **Spring Boot 3.5.3**
- **Spring Kafka**
- **Lombok**
- **Jackson** (processamento JSON)
- **Bean Validation**
- **Maven**

## Funcionalidades
- Simulação de consulta Serasa por CPF
- Processamento de mensagens síncronas via Kafka (Request-Reply)
- Base de dados mockada para testes
- Resposta automática para solicitações de verificação
- Acknowledgment manual para garantir processamento

## Configuração

### Kafka
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      retries: 3
    consumer:
      group-id: serasa-group
      auto-offset-reset: earliest
```

## Como Executar

### Pré-requisitos
- Java 21
- Apache Kafka (porta 9092)
- Maven

### Passos
1. Configure o Kafka na porta 9092
2. Execute o comando:
```bash
./mvnw spring-boot:run
```

O serviço estará disponível em: `http://localhost:8070`

## Tópicos Kafka

### Consumo
- **`verificar-serasa-request`**: Recebe solicitações de verificação de CPF

### Produção
- **`verificar-serasa-response`**: Envia resposta da consulta (automático via @SendTo)

## Estrutura do Projeto
```
src/main/java/com/example/serasa/
├── SerasaApplication.java           # Classe principal da aplicação
├── config/
│   ├── exception/
│   │   └── SerasaMessageException.java # Exceção para erros de mensageria
│   └── kafka/
│       └── KafkaConfig.java         # Configuração Kafka (Consumer/Producer)
├── constants/
│   └── TopicSerasa.java             # Constantes dos tópicos Kafka
└── service/
    ├── SerasaComsumerService.java   # Consumidor Kafka com padrão Request-Reply
    ├── SerasaService.java           # Serviço de negócio para consulta Serasa
    └── SerializationService.java    # Serviço de serialização/deserialização

src/main/resources/
└── application.yml                  # Configurações da aplicação
```

## Base de Dados Mockada
O serviço utiliza uma lista estática de CPFs para simular negativações:

```java
private static final Set<String> CPFS_NEGATIVADOS = Set.of(
    "11111111111",
    "22222222222"
);
```

### Lógica de Negócio
- CPFs na lista mockada retornam `true` (negativado)
- CPFs não listados retornam `false` (não negativado)
- Processamento automático de caracteres não numéricos

## Fluxo de Comunicação
1. **Recebimento**: Consome CPF do tópico `verificar-serasa-request`
2. **Processamento**: Verifica CPF na base mockada
3. **Resposta**: Envia resultado automaticamente para `verificar-serasa-response`
4. **Acknowledgment**: Confirma processamento manualmente

## Exemplo de Uso

### Request (CPF enviado no tópico)
```json
"11111111111"
```

### Response (retorno automático no tópico de resposta)
```json
"false"
```

### Para CPF negativado
```json
"22222222222                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    "
```
```json
"true"
```

## Configurações Kafka Específicas

### Consumer
- **Group ID**: `serasa-group`
- **Auto Offset Reset**: `earliest`
- **Enable Auto Commit**: `false` (acknowledgment manual)
- **Ack Mode**: `MANUAL_IMMEDIATE`

### Producer
- **Retries**: 3
- **Reply Template**: Configurado automaticamente


## Características Técnicas
- **Comunicação Síncrona**: Utiliza padrão request-reply do Kafka com @SendTo
- **Tolerância a Falhas**: Configurado com retry de 3 tentativas
- **Processamento**: Assíncrono através de listeners Kafka
- **Acknowledgment**: Manual imediato para garantir processamento
- **Serialização**: JSON via Jackson

## Configurações de Log
- Logs do Apache Kafka em nível INFO
- Logs do Hibernate SQL em nível DEBUG
- Logs customizados para consultas de CPF

## Integração
Este microsserviço é consumido pelo:
- **Microsserviço Pessoa**: Durante o processo de cadastro para verificar situação financeira

