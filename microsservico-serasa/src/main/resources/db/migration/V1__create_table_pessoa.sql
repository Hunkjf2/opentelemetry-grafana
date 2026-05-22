CREATE TABLE pessoa (
     id bigserial NOT NULL,
     nome varchar(150) NOT NULL,
     cpf varchar(11) NOT NULL,
     data_nascimento date NOT NULL,
     negativado boolean NULL,
     data_hora_criacao timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
     CONSTRAINT pessoa_pkey PRIMARY KEY (id)
);