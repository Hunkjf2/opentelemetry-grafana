package com.example.log.controller;

import com.example.log.dto.LogResponseDto;
import com.example.log.model.Log;
import com.example.log.service.log.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Logs", description = "Gerenciamento de logs de auditoria")
public class LogController {

    private final LogService logService;

    @GetMapping()
    @Operation(summary = "Listar todos os logs consultando o Redis",
            description = "Retorna todos os logs do sistema. Dados vêm do cache Redis para performance otimizada.")
    public ResponseEntity<List<LogResponseDto>> listarTodos() {
        log.info("Requisição recebida: GET /logs");

        List<Log> logs = logService.buscarTodosLogs();
        List<LogResponseDto> response = logs.stream()
                .map(LogResponseDto::from)
                .collect(Collectors.toList());

        log.info("Retornando {} logs", response.size());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica se o serviço está operacional")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Log Service is UP");
    }

}