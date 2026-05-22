package com.example.log.service.redis;

import com.example.log.config.redis.RedisProperties;
import com.example.log.constants.RedisKeys;
import com.example.log.model.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogRedisService {

    private final RedisService redisService;
    private final RedisProperties redisProperties;
    private final ObjectMapper objectMapper;

    public void saveLog(Log logPayload) {
        try {
            // 1. Salva log individual com chave: "log:ID"
            String logKey = buildLogKey(logPayload.getId());
            redisService.save(logKey, logPayload, redisProperties.getDefaultTtl());

            // 2. Adiciona ID à lista de todos os logs
            String allLogsKey = buildAllLogsKey();
            redisService.addToList(allLogsKey, logPayload.getId());

            // 3. Define TTL na lista (renovado a cada adição)
            redisService.expire(allLogsKey, redisProperties.getListTtl());

        } catch (Exception e) {
            log.error("Erro ao salvar log no Redis: id={}", logPayload.getId(), e);
        }
    }

    public Log getLogById(Long id) {
        try {
            String key = buildLogKey(id);
            Object cached = redisService.get(key);

            if (cached != null) {
                return objectMapper.convertValue(cached, Log.class);
            }

            log.debug("Cache miss para log id={}", id);
            return null;

        } catch (Exception e) {
            log.error("Erro ao buscar log do Redis: id={}", id, e);
            return null;
        }
    }

    public List<Log> getAllLogs() {
        try {
            String allLogsKey = buildAllLogsKey();

            // Busca lista de IDs (limitado por maxListSize)
            List<Object> ids = redisService.getList(
                    allLogsKey,
                    0,
                    redisProperties.getMaxListSize() - 1
            );

            if (ids == null || ids.isEmpty()) {
                return List.of();
            }

            // Busca cada log individualmente
            List<Log> logs = new ArrayList<>();
            for (Object idObj : ids) {
                Long id = objectMapper.convertValue(idObj, Long.class);
                Log log = getLogById(id);
                if (log != null) {
                    logs.add(log);
                }
            }

            log.info("Retornados {} logs do cache Redis", logs.size());
            return logs;
        } catch (Exception e) {
            log.error("Erro ao buscar todos os logs do Redis", e);
            return List.of();
        }
    }

    private String buildLogKey(Long id) {
        return redisProperties.getKeyPrefix() + RedisKeys.LOG_PREFIX + id;
    }

    private String buildAllLogsKey() {
        return redisProperties.getKeyPrefix() + RedisKeys.ALL_LOGS_LIST;
    }

}