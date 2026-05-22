package com.example.log.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PessoaDto(
        Long id,
        String nome,
        String cpf,
        LocalDate dataNascimento,
        Boolean negativado,
        LocalDateTime dataHoraCriacao
) {}