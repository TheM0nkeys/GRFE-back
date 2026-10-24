CREATE SEQUENCE funcionario_seq
    START WITH 1
    INCREMENT BY 50;


CREATE TABLE funcionario (

    id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,
    matricula VARCHAR(255) NOT NULL,
    email VARCHAR(255),

    CONSTRAINT pk_funcionario
    PRIMARY KEY (id),

    CONSTRAINT uq_funcionario_matricula
    UNIQUE (matricula)

);


CREATE TABLE funcionario_especialidades (

    funcionario_id BIGINT NOT NULL,
    especialidade_id BIGINT NOT NULL,

    CONSTRAINT pk_funcionario_especialidades
    PRIMARY KEY (funcionario_id, especialidade_id),

    CONSTRAINT fk_funcionario_especialidades_funcionario
    FOREIGN KEY (funcionario_id)
    REFERENCES funcionario (id),

    CONSTRAINT fk_funcionario_especialidades_especialidade
    FOREIGN KEY (especialidade_id)
    REFERENCES especialidades (id)

);