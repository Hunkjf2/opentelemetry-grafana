package com.example.pessoa.service.serasa;

import com.example.pessoa.service.metrics.MetricsService;
import com.example.pessoa.service.rabbitmq.RabbitMQSincronoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static com.example.pessoa.constants.serasa.TopicSerasa.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class SerasaService {

    private final RabbitMQSincronoService rabbitMQSincronoService;
    private final MetricsService metricsService;

    @CircuitBreaker(name = "microsservico-serasa", fallbackMethod = "fallbackConsultarSituacaoFinanceira")
    public Boolean consultarSituacaoFinanceira(String cpf) {
        String resultado = rabbitMQSincronoService.enviarEReceber(
                TOPIC_VERIFICAR_SERASA_REQUEST,
                cpf);
        log.info("Situação financeira consultada para CPF {}: {}", cpf, resultado);

        metricsService.registrarConsultaSerasa("sucesso");
        return Boolean.parseBoolean(resultado);
    }

    @SuppressWarnings("unused")
    public Boolean fallbackConsultarSituacaoFinanceira(String cpf, Exception ex) {
        log.warn("Fallback ativado para consulta do CPF {}: {}", cpf, ex.getMessage());
        metricsService.registrarConsultaSerasa("fallback");
        return null;
    }

}
