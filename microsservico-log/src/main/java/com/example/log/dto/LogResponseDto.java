package com.example.log.dto;

import java.time.LocalDateTime;

/**
 * DTO para resposta da API de logs.
 * Oculta detalhes internos da entidade.
 */
public record LogResponseDto(
        Long id,
        Long idUsuario,
        String nomeUsuario,
        String operacao,
        String dados,
        String nomeMicrosservico,
        LocalDateTime dataHoraCriacao
) {

    /**
     * Factory method para converter de entidade Log.
     *
     * @param log Entidade Log
     * @return DTO de resposta
     */
    public static LogResponseDto from(com.example.log.model.Log log) {
        return new LogResponseDto(
                log.getId(),
                log.getIdUsuario(),
                log.getNomeUsuario(),
                log.getOperacao(),
                log.getDados(),
                log.getNomeMicroSservico(),
                log.getDataHoraCriacao()
        );
    }

}