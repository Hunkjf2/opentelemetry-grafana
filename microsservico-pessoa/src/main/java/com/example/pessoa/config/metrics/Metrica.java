package com.example.pessoa.config.metrics;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Metrica {

    String nome();
    String descricao() default "";
    String operacao() default "";
}