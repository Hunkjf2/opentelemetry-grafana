package com.example.serasa.service;

import com.example.serasa.config.exception.SerasaMessageException;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import static com.example.serasa.constants.TopicSerasa.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class SerasaConsumerService {

    private final SerasaService serasaService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = TOPIC_VERIFICAR_SERASA_REQUEST)
    public void consultarCpf(Message message, Channel channel) {
        try {

            String cpf = new String(message.getBody());
            boolean resultado = serasaService.consultarCpfSerasa(cpf);

            log.info("Resultado da consulta para CPF {}: {}", cpf, resultado);

            // Envia a resposta para a fila de resposta usando reply-to e correlation-id
            String replyTo = message.getMessageProperties().getReplyTo();
            String correlationId = message.getMessageProperties().getCorrelationId();

            if (replyTo != null) {
                MessageProperties replyProperties = new MessageProperties();
                replyProperties.setCorrelationId(correlationId);

                Message replyMessage = new Message(
                    String.valueOf(resultado).getBytes(),
                    replyProperties
                );

                rabbitTemplate.send(replyTo, replyMessage);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            throw new SerasaMessageException("Falha no processamento:", e);
        }
    }

}