package com.example.pessoa.dto;

public record LogEventDto(
        PessoaDto pessoaDto,
        String operacao,
        String microservico,
        Long idUsuario,
        String nomeUsuario
) {
}
