package com.example.pessoa.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public void registrarConsultaSerasa(String resultado) {
        Counter.builder("serasa.consultas.total")
                .description("Total de consultas ao Serasa (sucesso | fallback)")
                .tag("resultado", resultado)
                .register(meterRegistry)
                .increment();
    }

    // Métricas de RabbitMQ — contexto específico, mantém aqui
    public void registrarMensagemAssincrona(String fila) {
        Counter.builder("rabbitmq.mensagens.assincrono.total")
                .description("Total de mensagens enviadas de forma assíncrona")
                .tag("fila", fila)
                .register(meterRegistry)
                .increment();
    }

    public void registrarMensagemSincrona(String fila, String resultado) {
        Counter.builder("rabbitmq.mensagens.sincrono.total")
                .description("Total de mensagens RPC enviadas ao RabbitMQ")
                .tag("fila", fila)
                .tag("resultado", resultado)
                .register(meterRegistry)
                .increment();
    }

    public Timer.Sample iniciarTimer() {
        return Timer.start(meterRegistry);
    }

    public void registrarDuracaoSincrona(Timer.Sample sample, String fila, String resultado) {
        sample.stop(
                Timer.builder("rabbitmq.sincrono.duracao")
                        .description("Duração das chamadas RPC síncronas ao RabbitMQ")
                        .tag("fila", fila)
                        .tag("resultado", resultado)
                        .register(meterRegistry)
        );
    }
}
