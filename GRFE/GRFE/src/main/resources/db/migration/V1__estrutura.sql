CREATE SEQUENCE divisao_seq
    START WITH 1
    INCREMENT BY 50;

CREATE SEQUENCE departamento_seq
    START WITH 1
    INCREMENT BY 50;

CREATE SEQUENCE especialidades_seq
    START WITH 1
    INCREMENT BY 50;


CREATE TABLE divisao (

    id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,

    CONSTRAINT pk_divisao
    PRIMARY KEY (id)

);


CREATE TABLE departamento (

    id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,
    divisao_id BIGINT NOT NULL,

    CONSTRAINT pk_departamento
    PRIMARY KEY (id),

    CONSTRAINT fk_departamento_divisao
    FOREIGN KEY (divisao_id)
    REFERENCES divisao (id)

);


CREATE TABLE especialidades (

    id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,
    descricao VARCHAR(255),
    divisao_id BIGINT NOT NULL,
    departamento_id BIGINT NOT NULL,

    CONSTRAINT pk_especialidades
    PRIMARY KEY (id),

    CONSTRAINT fk_especialidades_divisao
    FOREIGN KEY (divisao_id)
    REFERENCES divisao (id),

    CONSTRAINT fk_especialidades_departamento
    FOREIGN KEY (departamento_id)
    REFERENCES departamento (id)

);