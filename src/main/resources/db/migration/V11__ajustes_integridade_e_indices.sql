ALTER TABLE usuario
    ADD CONSTRAINT uk_usuario_funcionario UNIQUE (funcionario_id);

CREATE INDEX idx_chamado_data_hora_acionamento
    ON chamado(data_hora_acionamento);
