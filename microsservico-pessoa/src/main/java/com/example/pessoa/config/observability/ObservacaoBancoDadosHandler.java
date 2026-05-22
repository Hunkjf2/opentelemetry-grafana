package com.example.pessoa.config.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Intercepta todas as observações JDBC geradas pela lib {@code datasource-micrometer}
 * e emite logs estruturados em cada fase do ciclo de vida de uma operação no banco.
 *
 * <p>O Micrometer dispara os eventos nesta ordem para cada query SQL:</p>
 * <pre>
 *   onStart  → query submetida ao banco
 *   onStop   → query concluída (sucesso)
 *   onError  → query concluída (falha)
 * </pre>
 *
 * <p>As observações interceptadas possuem os nomes:</p>
 * <ul>
 *   <li>{@code jdbc.query}      – execução de uma query SQL</li>
 *   <li>{@code jdbc.connection} – ciclo de vida de uma conexão JDBC</li>
 * </ul>
 *
 * <p>Além dos logs, cada observação gera automaticamente:</p>
 * <ul>
 *   <li>Uma métrica {@code jdbc.query} no Grafana (via OTLP)</li>
 *   <li>Um span filho no Grafana Tempo ligado ao trace da requisição HTTP pai</li>
 * </ul>
 */
@Slf4j
@Component
public class ObservacaoBancoDadosHandler implements ObservationHandler<Observation.Context> {

    private static final String PREFIXO_JDBC = "jdbc";

    @Override
    public void onStart(Observation.Context context) {
        log.info(
                "[ JDBC ] >> INICIO    | operacao='{}' | detalhes={}",
                context.getName(),
                context.getLowCardinalityKeyValues()
        );
    }

    @Override
    public void onStop(Observation.Context context) {
        log.info(
                "[ JDBC ] OK CONCLUSAO | operacao='{}' | detalhes={}",
                context.getName(),
                context.getLowCardinalityKeyValues()
        );
    }

    @Override
    public void onError(Observation.Context context) {
        log.error(
                "[ JDBC ] XX ERRO      | operacao='{}' | causa='{}'",
                context.getName(),
                context.getError() != null ? context.getError().getMessage() : "desconhecido"
        );
    }

    /**
     * Filtra apenas observações JDBC, evitando que este handler processe
     * observações de HTTP, RabbitMQ ou qualquer outra origem.
     */
    @Override
    public boolean supportsContext(Observation.Context context) {
        return context.getName() != null
                && context.getName().startsWith(PREFIXO_JDBC);
    }
}
