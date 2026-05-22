package com.example.log.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log")
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "nome_usuario", nullable = false, length = 100)
    private String nomeUsuario;

    private String operacao;
    private String dados;

    @Column(name = "nome_microsservico", nullable = false, length = 60)
    private String nomeMicroSservico;

    @Column(name = "data_hora_criacao", nullable = false)
    private LocalDateTime dataHoraCriacao;

}