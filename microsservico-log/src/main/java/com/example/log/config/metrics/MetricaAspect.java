package com.example.log.config.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspecto que intercepta métodos anotados com {@link Metrica} e registra
 * automaticamente contador (sucesso/erro) e timer de duração no Micrometer.
 *
 * <p>Elimina a necessidade de instrumentação manual em cada método de serviço,
 * centralizando toda a lógica de métricas neste único ponto.</p>
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricaAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(com.example.pessoa.config.metrics.Metrica)")
    public Object interceptar(ProceedingJoinPoint joinPoint) throws Throwable {
        Metrica metrica = obterAnotacao(joinPoint);
        Timer.Sample timer = Timer.start(meterRegistry);
        String resultado = "sucesso";
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            resultado = "erro";
            throw e;
        } finally {
            registrarContador(metrica, resultado);
            registrarTimer(metrica, resultado, timer);
        }
    }

    private void registrarContador(Metrica metrica, String resultado) {
        Counter.builder(metrica.nome() + ".total")
                .description(metrica.descricao())
                .tag("resultado", resultado)
                .register(meterRegistry)
                .increment();
    }

    private void registrarTimer(Metrica metrica, String resultado, Timer.Sample timer) {
        String operacao = metrica.operacao().isBlank()
                ? metrica.nome()
                : metrica.operacao();

        timer.stop(
                Timer.builder(metrica.nome() + ".duracao")
                        .description("Duração de " + metrica.descricao())
                        .tag("operacao", operacao)
                        .tag("resultado", resultado)
                        .register(meterRegistry)
        );
    }

    private Metrica obterAnotacao(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget()
                .getClass()
                .getMethod(signature.getName(), signature.getParameterTypes());
        return method.getAnnotation(Metrica.class);
    }
}