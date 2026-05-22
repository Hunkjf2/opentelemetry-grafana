package com.example.log.constants;

import lombok.experimental.UtilityClass;

/**
 * Constantes para chaves Redis.
 * Centraliza nomenclatura para evitar erros de digitação.
 */
@UtilityClass
public class RedisKeys {

    /**
     * Chave para lista de todos os logs.
     * Estrutura: List do Redis contendo IDs de logs
     */
    public static final String ALL_LOGS_LIST = "all";

    /**
     * Prefixo para log individual.
     * Uso: LOG_PREFIX + id → "log:123"
     */
    public static final String LOG_PREFIX = "log:";

    /**
     * Chave para contador de logs (opcional, para estatísticas).
     */
    public static final String LOGS_COUNT = "count";

    /**
     * Prefixo para logs por operação.
     * Uso: LOGS_BY_OPERATION + operacao → "operation:CADASTRO"
     */
    public static final String LOGS_BY_OPERATION = "operation:";

    /**
     * Prefixo para logs por microsserviço.
     * Uso: LOGS_BY_SERVICE + service → "service:microservico-pessoa"
     */
    public static final String LOGS_BY_SERVICE = "service:";

}