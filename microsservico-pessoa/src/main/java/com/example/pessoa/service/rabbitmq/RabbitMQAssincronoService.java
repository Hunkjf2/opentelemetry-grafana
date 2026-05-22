package com.example.pessoa.service.rabbitmq;

import com.example.pessoa.config.exception.ProcessingException;
import com.example.pessoa.service.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQAssincronoService {

    private final RabbitTemplate rabbitTemplate;
    private final MetricsService metricsService;

    public void enviar(String queue, Object payload) {
        try {
            rabbitTemplate.convertAndSend(queue, payload);
            log.info("Mensagem enviada para fila: {} Mensagem: {} ", queue, payload);
            metricsService.registrarMensagemAssincrona(queue);
        } catch (Exception e) {
            throw new ProcessingException("Falha no envio do evento:", e);
        }
    }
}
