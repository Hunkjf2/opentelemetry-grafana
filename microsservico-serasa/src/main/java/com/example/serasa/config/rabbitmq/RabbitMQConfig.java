package com.example.serasa.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.serasa.constants.TopicSerasa.*;

@Configuration
public class RabbitMQConfig {

    /**
     * Configuração do MessageConverter para serializar/deserializar mensagens JSON
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Cria um RabbitTemplate para envio de mensagens.
     *
     * @return Template configurado para envio de mensagens
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    /**
     * Define a fila de requisição para consulta do Serasa.
     * Esta fila recebe as solicitações de consulta de CPF.
     *
     * @return Queue configurada para requisições
     */
    @Bean
    public Queue serasaRequestQueue() {
        return new Queue(TOPIC_VERIFICAR_SERASA_REQUEST, true);
    }

    /**
     * Define a fila de resposta para consulta do Serasa.
     * Esta fila recebe as respostas da consulta de CPF.
     *
     * @return Queue configurada para respostas
     */
    @Bean
    public Queue serasaResponseQueue() {
        return new Queue(TOPIC_VERIFICAR_SERASA_RESPONSE, true);
    }

    /**
     * Define a exchange do tipo Direct para roteamento de mensagens.
     * Utiliza routing keys para direcionar mensagens para as filas corretas.
     *
     * @return DirectExchange configurada
     */
    @Bean
    public DirectExchange serasaExchange() {
        return new DirectExchange("serasa.exchange");
    }

    /**
     * Binding entre a exchange e a fila de requisição.
     * Mensagens com a routing key 'serasa.request' serão roteadas para a fila de requisição.
     *
     * @return Binding configurado
     */
    @Bean
    public Binding serasaRequestBinding(Queue serasaRequestQueue, DirectExchange serasaExchange) {
        return BindingBuilder.bind(serasaRequestQueue)
                .to(serasaExchange)
                .with("serasa.request");
    }

    /**
     * Binding entre a exchange e a fila de resposta.
     * Mensagens com a routing key 'serasa.response' serão roteadas para a fila de resposta.
     *
     * @return Binding configurado
     */
    @Bean
    public Binding serasaResponseBinding(Queue serasaResponseQueue, DirectExchange serasaExchange) {
        return BindingBuilder.bind(serasaResponseQueue)
                .to(serasaExchange)
                .with("serasa.response");
    }

    /**
     * Configuração do listener container para consumir mensagens de requisição.
     * Permite controle manual de acknowledgment das mensagens.
     *
     * @return SimpleRabbitListenerContainerFactory configurado
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

}
