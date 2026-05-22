package com.example.pessoa.mapper;

import com.example.pessoa.dto.PessoaDto;
import com.example.pessoa.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class PessoaMapper {

    public Pessoa toEntity(PessoaDto dto) {
        return Pessoa.builder()
                .id(dto.id())
                .nome(dto.nome())
                .cpf(dto.cpf())
                .dataNascimento(dto.dataNascimento())
                .negativado(dto.negativado())
                .dataHoraCriacao(dto.dataHoraCriacao())
                .build();
    }

    public PessoaDto toDto(Pessoa pessoa) {
        return new PessoaDto(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.getDataNascimento(),
                pessoa.getNegativado(),
                pessoa.getDataHoraCriacao()
        );
    }

}