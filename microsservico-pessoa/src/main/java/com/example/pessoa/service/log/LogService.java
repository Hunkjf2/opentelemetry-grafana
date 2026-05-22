package com.example.pessoa.service.log;

import static com.example.pessoa.constants.log.TopicLog.*;
import com.example.pessoa.dto.LogEventDto;
import com.example.pessoa.mapper.PessoaMapper;
import com.example.pessoa.model.Pessoa;
import com.example.pessoa.service.rabbitmq.RabbitMQAssincronoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LogService {

    private final RabbitMQAssincronoService rabbitMQAssincronoService;
    private final PessoaMapper pessoaMapper;

    public void enviarDadosLog(Pessoa pessoa, String operacao) {
        LogEventDto logEvent = new LogEventDto(
                pessoaMapper.toDto(pessoa),
                operacao,
                "microservico-pessoa",
                1L,
                "Jhon Doe"
        );

        rabbitMQAssincronoService.enviar(TOPIC_ENVIAR_LOG, logEvent);
    }


}
