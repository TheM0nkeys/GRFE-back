CREATE SEQUENCE chamado_seq
    START WITH 1
    INCREMENT BY 50;

CREATE SEQUENCE historico_acionamento_seq
    START WITH 1
    INCREMENT BY 50;


CREATE TABLE chamado (

    id BIGINT NOT NULL,
    data_hora_acionamento TIMESTAMP NOT NULL,
    especialidade_id BIGINT NOT NULL,
    plantonista_id BIGINT NOT NULL,
    usuario_responsavel_id BIGINT NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    numero_incidente VARCHAR(255),
    status VARCHAR(255) NOT NULL,

    CONSTRAINT pk_chamado
    PRIMARY KEY (id),

    CONSTRAINT fk_chamado_especialidade
    FOREIGN KEY (especialidade_id)
    REFERENCES especialidades (id),

    CONSTRAINT fk_chamado_plantonista
    FOREIGN KEY (plantonista_id)
    REFERENCES funcionario (id),

    CONSTRAINT fk_chamado_usuario_responsavel
    FOREIGN KEY (usuario_responsavel_id)
    REFERENCES funcionario (id)

);


CREATE TABLE historico_acionamento (

    id BIGINT NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    autor_id BIGINT NOT NULL,
    comentario VARCHAR(2000) NOT NULL,
    chamado_id BIGINT NOT NULL,

    CONSTRAINT pk_historico_acionamento
    PRIMARY KEY (id),

    CONSTRAINT fk_historico_autor
    FOREIGN KEY (autor_id)
    REFERENCES funcionario (id),

    CONSTRAINT fk_historico_chamado
    FOREIGN KEY (chamado_id)
    REFERENCES chamado (id)
);