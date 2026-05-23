package com.example.log.service.log;

import com.example.log.config.exception.LogConsultaException;
import com.example.log.model.Log;
import com.example.log.repository.LogRepository;
import com.example.log.service.redis.LogRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogCacheService {

    private final LogRedisService logRedisService;
    private final LogRepository logRepository;

    public List<Log> buscarComCacheAside() {
        try {
            List<Log> logsFromCache = logRedisService.getAllLogs();

            if (!logsFromCache.isEmpty()) {
                log.info("Cache hit — {} logs retornados do Redis", logsFromCache.size());
                return logsFromCache;
            }

            return buscarDoBancoEPopularCache();
        } catch (Exception e) {
            throw new LogConsultaException("Falha ao buscar logs", e);
        }
    }

    private List<Log> buscarDoBancoEPopularCache() {
        log.info("Cache miss — buscando logs do PostgreSQL");
        List<Log> logsFromDb = logRepository.findAll();

        if (!logsFromDb.isEmpty()) {
            logsFromDb.forEach(logRedisService::saveLog);
            log.info("Cache populado com {} logs do PostgreSQL", logsFromDb.size());
        }

        return logsFromDb;
    }
}