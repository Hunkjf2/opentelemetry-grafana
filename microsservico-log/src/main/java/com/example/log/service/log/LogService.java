package com.example.log.service.log;

import com.example.log.config.exception.LogCadastroException;
import com.example.log.config.metrics.Loggable;
import com.example.log.config.metrics.Metrica;
import com.example.log.model.Log;
import com.example.log.repository.LogRepository;
import com.example.log.service.redis.LogRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogService {

    private final LogRepository logRepository;
    private final LogRedisService logRedisService;
    private final LogCacheService logCacheService;


    @Transactional
    @Loggable(operacao = "CADASTRO_LOGS")
    @Metrica(
            nome = "log.cadastros",
            descricao = "Cadastros de Logs",
            operacao = "cadastro"
    )
    public void cadastrarLog(Log logPayload) {
        try {
            Log logSalvo = logRepository.save(logPayload);
            log.info("Log salvo no PostgreSQL: id={}", logSalvo.getId());
            logRedisService.saveLog(logSalvo);
        } catch (Exception e) {
            throw new LogCadastroException("Falha ao persistir log no banco de dados", e);
        }
    }

    @Metrica(
            nome = "log.consultas",
            descricao = "Cadastros de Logs",
            operacao = "cadastro"
    )
    public List<Log> buscarTodosLogs() {
        return logCacheService.buscarComCacheAside();
    }

}