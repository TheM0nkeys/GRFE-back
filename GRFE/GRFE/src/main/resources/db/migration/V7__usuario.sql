CREATE SEQUENCE usuario_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE usuario (

    id BIGINT NOT NULL PRIMARY KEY,
    login VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    funcionario_id BIGINT,
    CONSTRAINT fk_usuario_funcionario FOREIGN KEY (funcionario_id) REFERENCES funcionario(id)

);

  CREATE INDEX idx_usuario_funcionario ON usuario(funcionario_id);