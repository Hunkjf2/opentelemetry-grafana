package com.example.pessoa.config.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura o MeterRegistry do Micrometer aplicando tags comuns a TODAS
 * as métricas exportadas, garantindo rastreabilidade no Grafana.
 *
 * <p>As tags definidas aqui enriquecem automaticamente cada série temporal,
 * permitindo filtrar por aplicação e ambiente nos dashboards.</p>
 */
@Configuration
public class ObservabilityConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configurarTagsGlobais() {
        return registry -> registry.config()
                .commonTags(
                        "aplicacao", applicationName
                );
    }
}
