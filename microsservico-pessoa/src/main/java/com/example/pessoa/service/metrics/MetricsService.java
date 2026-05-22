package com.example.pessoa.service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Serviço centralizado de métricas de negócio via Micrometer.
 *
 * <p>Toda instrumentação personalizada passa por aqui, mantendo os serviços
 * de domínio desacoplados da API do Micrometer e facilitando a manutenção.</p>
 *
 * <p><b>Métricas expostas:</b></p>
 * <ul>
 *   <li>{@code pessoa.cadastros.total}         – contador de cadastros por resultado</li>
 *   <li>{@code pessoa.edicoes.total}            – contador de edições por resultado</li>
 *   <li>{@code pessoa.exclusoes.total}          – contador de exclusões por resultado</li>
 *   <li>{@code pessoa.operacoes.duracao}        – timer das operações de domínio</li>
 *   <li>{@code serasa.consultas.total}          – consultas ao Serasa (sucesso/fallback)</li>
 *   <li>{@code rabbitmq.mensagens.assincrono.total} – mensagens publicadas sem resposta</li>
 *   <li>{@code rabbitmq.mensagens.sincrono.total}   – mensagens RPC com resultado</li>
 *   <li>{@code rabbitmq.sincrono.duracao}       – timer das chamadas RPC ao RabbitMQ</li>
 * </ul>
 *
 * <p>Todas as métricas herdam as tags globais definidas em {@code ObservabilityConfig}
 * ({@code aplicacao}, {@code environment}) e são exportadas via OTLP para o Grafana.</p>
 */
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // =========================================================
    // Métricas de Pessoa
    // =========================================================

    public void registrarCadastro(String resultado) {
        contarOperacao(
                "pessoa.cadastros.total",
                "Total de cadastros de pessoa realizados",
                resultado
        );
    }

    public void registrarEdicao(String resultado) {
        contarOperacao(
                "pessoa.edicoes.total",
                "Total de edições de pessoa realizadas",
                resultado
        );
    }

    public void registrarExclusao(String resultado) {
        contarOperacao(
                "pessoa.exclusoes.total",
                "Total de exclusões de pessoa realizadas",
                resultado
        );
    }

    // =========================================================
    // Timer de operações de domínio
    // =========================================================

    public Timer.Sample iniciarTimer() {
        return Timer.start(meterRegistry);
    }

    public void registrarDuracao(Timer.Sample sample, String operacao, String resultado) {
        sample.stop(
                Timer.builder("pessoa.operacoes.duracao")
                        .description("Duração das operações do microsserviço de pessoa em segundos")
                        .tag("operacao", operacao)
                        .tag("resultado", resultado)
                        .register(meterRegistry)
        );

    }

    // =========================================================
    // Métricas de Serasa
    // =========================================================

    public void registrarConsultaSerasa(String resultado) {
        contarOperacao(
                "serasa.consultas.total",
                "Total de consultas ao Serasa (sucesso | fallback)",
                resultado
        );
    }

    // =========================================================
    // Métricas de RabbitMQ
    // =========================================================

    public void registrarMensagemAssincrona(String fila) {
        Counter.builder("rabbitmq.mensagens.assincrono.total")
                .description("Total de mensagens enviadas de forma assíncrona ao RabbitMQ")
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

    public void registrarDuracaoSincrona(Timer.Sample sample, String fila, String resultado) {
        sample.stop(
                Timer.builder("rabbitmq.sincrono.duracao")
                        .description("Duração das chamadas RPC síncronas ao RabbitMQ em segundos")
                        .tag("fila", fila)
                        .tag("resultado", resultado)
                        .register(meterRegistry)
        );
    }

    // =========================================================
    // Privado
    // =========================================================

    private void contarOperacao(String nome, String descricao, String resultado) {
        Counter.builder(nome)
                .description(descricao)
                .tag("resultado", resultado)
                .register(meterRegistry)
                .increment();
    }
}
