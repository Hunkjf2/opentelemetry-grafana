package com.example.log.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LogDto(
        Long idUsuario,
        String nomeUsuario,
        String operacao,
        String nomeMicroSservico,
        LocalDateTime dataHoraCriacao
) {}