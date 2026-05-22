package com.example.pessoa.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pessoa")
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    private Boolean negativado;

    @CreationTimestamp
    @Column(name = "data_hora_criacao", nullable = false,  updatable = false)
    private LocalDateTime dataHoraCriacao;

}