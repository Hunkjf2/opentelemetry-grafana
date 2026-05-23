package com.example.log.service.log;

import com.example.log.config.exception.ProcessingException;
import com.example.log.dto.LogEventDto;
import com.example.log.dto.PessoaDto;
import com.example.log.model.Log;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import static com.example.log.constants.TopicLog.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class LogConsumerService {

    private final LogService logService;
    private final ObjectMapper objectMapper;


    @RabbitListener(queues = TOPIC_ENVIAR_LOG)
    public void processarEnvioLog(LogEventDto logEventDto, Channel channel, Message message) {
        try {
            log.info("Recebendo mensagem de log para processamento: operacao={}, microservico={}",
                    logEventDto.operacao(), logEventDto.microservico());

            PessoaDto pessoaDto = logEventDto.pessoaDto();
            String mensagemJson = objectMapper.writeValueAsString(logEventDto);

            Log logObject = Log.builder()
                    .operacao(logEventDto.operacao())
                    .dados(mensagemJson)
                    .dataHoraCriacao(pessoaDto.dataHoraCriacao())
                    .nomeUsuario(logEventDto.nomeUsuario())
                    .nomeMicroSservico(logEventDto.microservico())
                    .idUsuario(pessoaDto.id())
                    .build();

            logService.cadastrarLog(logObject);

            log.info("Log processado com sucesso: idUsuario={}, operacao={}",
                    pessoaDto.id(), logEventDto.operacao());

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (Exception e) {
            log.error("Erro ao processar mensagem de cadastro de log", e);

            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception nackError) {
                log.error("Erro ao enviar NACK", nackError);
            }

            throw new ProcessingException("Erro ao processar mensagem de cadastro de log", e);
        }
    }

}