package com.example.pessoa.service.rabbitmq;

import com.example.pessoa.config.exception.ProcessingException;
import com.example.pessoa.service.metrics.MetricsService;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RabbitMQSincronoService {

    private final RabbitTemplate rabbitTemplate;
    private final MetricsService metricsService;

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    public String enviarEReceber(String queue, String cpf) {
        Timer.Sample timer = metricsService.iniciarTimer();
        String resultado = "sucesso";
        try {
            rabbitTemplate.setReplyTimeout(TIMEOUT.toMillis());

            Object response = rabbitTemplate.convertSendAndReceive(queue, cpf);

            if (response == null) {
                resultado = "timeout";
                throw new ProcessingException("Timeout ou nenhuma resposta recebida", null);
            }

            return response.toString();
        } catch (Exception e) {
            if ("sucesso".equals(resultado)) resultado = "erro";
            throw new ProcessingException("Falha no envio e recebimento da mensagem:", e);
        } finally {
            metricsService.registrarMensagemSincrona(queue, resultado);
            metricsService.registrarDuracaoSincrona(timer, queue, resultado);
        }
    }

}
