CREATE SEQUENCE escala_seq
    START WITH 1
    INCREMENT BY 50;


CREATE TABLE escala (

    id BIGINT NOT NULL,
    data_hora_inicio TIMESTAMP NOT NULL,
    data_hora_fim TIMESTAMP NOT NULL,
    especialidade_id BIGINT NOT NULL,
    funcionario_id BIGINT NOT NULL,

    CONSTRAINT pk_escala
    PRIMARY KEY (id),

    CONSTRAINT fk_escala_especialidade
    FOREIGN KEY (especialidade_id)
    REFERENCES especialidades (id),

    CONSTRAINT fk_escala_funcionario
    FOREIGN KEY (funcionario_id)
    REFERENCES funcionario (id)
);