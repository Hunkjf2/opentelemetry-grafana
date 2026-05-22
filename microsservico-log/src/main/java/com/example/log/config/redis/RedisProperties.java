package com.example.log.config.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades customizadas para configuração do Redis.
 * Permite externalizar configurações específicas da aplicação.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.redis")
public class RedisProperties {

    /**
     * Prefixo para todas as chaves Redis da aplicação.
     * Evita colisões com outras aplicações no mesmo Redis.
     */
    private String keyPrefix = "log:";

    /**
     * TTL padrão para cache em segundos.
     * 86400 segundos = 24 horas
     */
    private long defaultTtl = 86400;

    /**
     * TTL para lista de logs em segundos.
     * 3600 segundos = 1 hora (atualiza mais frequentemente)
     */
    private long listTtl = 3600;

    /**
     * Número máximo de logs na lista (para paginação/limite).
     */
    private int maxListSize = 1000;

}