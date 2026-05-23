package com.example.log.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(LogCadastroException.class)
    public ProblemDetail handleLogCadastroException(LogCadastroException ex) {
        log.error("Erro ao cadastrar log: {}", ex.getMessage(), ex);
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao cadastrar log", ex);
    }

    @ExceptionHandler(LogConsultaException.class)
    public ProblemDetail handleLogConsultaException(LogConsultaException ex) {
        log.error("Erro ao consultar logs: {}", ex.getMessage(), ex);
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao consultar logs", ex);
    }

    @ExceptionHandler(ProcessingException.class)
    public ProblemDetail handleProcessingException(ProcessingException ex) {
        log.error("Erro de processamento: {}", ex.getMessage(), ex);
        return buildProblemDetail(HttpStatus.UNPROCESSABLE_ENTITY, "Erro de processamento de mensagem", ex);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado", ex);
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String title, Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setTitle(title);
        problem.setType(URI.create("about:blank"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}