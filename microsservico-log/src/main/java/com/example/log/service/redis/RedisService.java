package com.example.log.service.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Serviço genérico para operações Redis.
 * Fornece métodos reutilizáveis para qualquer tipo de dado.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Salva um valor com TTL.
     *
     * @param key Chave Redis
     * @param value Valor a ser armazenado
     * @param ttl Tempo de vida em segundos
     */
    public void save(String key, Object value, long ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
            log.debug("Salvo no Redis: key={}, ttl={}s", key, ttl);
        } catch (Exception e) {
            log.error("Erro ao salvar no Redis: key={}", key, e);
            // Não propaga exceção - Redis é cache, não deve quebrar fluxo principal
        }
    }

    /**
     * Busca um valor por chave.
     *
     * @param key Chave Redis
     * @return Valor encontrado ou null
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Erro ao buscar do Redis: key={}", key, e);
            return null;
        }
    }

    /**
     * Adiciona item a uma lista Redis (LPUSH).
     *
     * @param key Chave da lista
     * @param value Valor a adicionar
     */
    public void addToList(String key, Object value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            log.debug("Adicionado à lista Redis: key={}", key);
        } catch (Exception e) {
            log.error("Erro ao adicionar à lista Redis: key={}", key, e);
        }
    }

    /**
     * Busca todos os itens de uma lista.
     *
     * @param key Chave da lista
     * @param start Índice inicial
     * @param end Índice final (-1 para todos)
     * @return Lista de valores
     */
    public List<Object> getList(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Erro ao buscar lista do Redis: key={}", key, e);
            return List.of();
        }
    }

    /**
     * Define TTL em uma chave existente.
     *
     * @param key Chave
     * @param ttl Tempo de vida em segundos
     */
    public void expire(String key, long ttl) {
        try {
            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Erro ao definir TTL no Redis: key={}", key, e);
        }
    }

}