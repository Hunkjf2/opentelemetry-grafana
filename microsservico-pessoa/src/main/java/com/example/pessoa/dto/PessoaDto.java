package com.example.pessoa.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PessoaDto(
        Long id,

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 150, message = "Nome deve ter entre 2 a 150 caracteres")
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF deve ter formato válido")
        String cpf,

        @NotNull(message = "Data de Nascimento é obrigatória")
        @Past(message = "Data de nascimento deve ser no passado")
        LocalDate dataNascimento,

        Boolean negativado,

        LocalDateTime dataHoraCriacao
) {}