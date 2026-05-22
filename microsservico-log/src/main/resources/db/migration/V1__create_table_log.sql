CREATE TABLE log (
    id BIGSERIAL PRIMARY KEY,
    id_usuario bigint NOT NULL,
    nome_usuario VARCHAR(100) NOT NULL,
    operacao VARCHAR(30) NOT NULL,
    dados TEXT NOT NULL,
    nome_microsservico VARCHAR(60) NOT NULL,
    data_hora_criacao TIMESTAMP NOT NULL
);